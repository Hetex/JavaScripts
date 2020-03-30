import com.sun.deploy.util.ArrayUtil;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import util.DefaultAPI;
import util.ReactionGenerator;
import util.Settings;
import util.Sleep;
import util.ge.ExchangeItem;
import util.ge.RSExchange;
import util.mouse.MouseCursor;
import util.mouse.MouseTrail;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ScriptManifest(version = 1, logo = "", author = "removed", info = "Hunter Boi", name = "SHunter")
public class Main extends Script {

    private DefaultAPI api;
    private Long startTime;
    private States status = States.INIT;

    private Area trapArea = null;
    private List<Position> trapLocations = new ArrayList<>();

    private Entity nextTrap = null;
    private Entity currentTrap = null;
    private final RSExchange rsExchange = new RSExchange();

    private int chinsCaught = 0;
    private int chinPrice = 1500;
    private int profit = 0;

    int boxTrap = 10008;
    int emptyTrap = 9385;
    int shakingTrap = 9383;
    int setTrap = 9380;

    int trapCount = 0;

    boolean isResetingTrap = false;

    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.white, this);

    @Override
    public final void onStart() throws InterruptedException {

        startTime = System.currentTimeMillis();
        getExperienceTracker().start(Skill.HUNTER);
        api = new DefaultAPI(this.bot, new Settings(), new ReactionGenerator());
        trapArea = api.myPlayer().getArea(1);


        Map<String, ExchangeItem> exchangeItems = new RSExchange().getExchangeItems("Red chinchompa");
        if(exchangeItems.get("Red chinchompa").getBuyAverage() != -1)
        {
            chinPrice = exchangeItems.get("Red chinchompa").getBuyAverage();
        }

        //Initialise all trap locations
        //doesn't work for unset trap...
        List<Integer> allTraps = new ArrayList<>();
        allTraps.add(boxTrap);
        allTraps.add(emptyTrap);
        allTraps.add(shakingTrap);
        allTraps.add(setTrap);

        List<RS2Object> traps = getObjects().filter(obj -> allTraps.contains(obj.getId()) && trapArea.contains(obj));

        if(traps.size() != 5)
            log("Found " + trapLocations.size() + " traps.");

        for(RS2Object trap : traps)
        {
            trapLocations.add(trap.getPosition());
        }
    }

    @Override
    public final void onMessage(final Message message) {
        if (message.getType() == Message.MessageType.GAME) {
            if (message.getMessage().contains("setting up the trap")) {
                isResetingTrap = false;
                trapCount++;
            } else if (message.getMessage().contains("dismantle the trap")) {
                isResetingTrap = true;
                trapCount--;
            } else if (message.getMessage().contains("that you set has collapsed")) {
                trapCount--;
            } else if (message.getMessage().contains("caught")){
                chinsCaught++;
                profit = chinsCaught * chinPrice;
            } else if(message.getMessage().contains("isn't your") || message.getMessage().contains("only 5 trap"))
            {
                stop(false);
                Toolkit.getDefaultToolkit().beep();
                try {
                    MethodProvider.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPaint(final Graphics2D g) {
        if (api == null)
            return;

        if (api.getMouse().isOnScreen()) {
            trail.paint(g);
            cursor.paint(g);
        }

        int x = 15;
        int y = 270;

        Font font = new Font("Open Sans", Font.BOLD, 10);
        g.setFont(font);
        g.setColor(Color.yellow);
        g.drawString("Run time: " + (api.formatTime(System.currentTimeMillis() - startTime)), x, y);
        g.drawString("Status: " + status, x, y + 10);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.HUNTER) != 0)
            g.drawString("Hunter xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.HUNTER) + " TTL: " + api.formatTime(api.getExperienceTracker().getTimeToLevel(Skill.HUNTER)), x, y + 20);
        g.drawString("Chinchompa caught: " + chinsCaught, x, y + 30);
        g.drawString("Profit: " + profit + " profit/h: " + (long) (profit * (3600000.0 / (System.currentTimeMillis() - startTime))), x, y + 40);

        g.drawString("Sleeping for: " + (api.settings.getTimeRemaining() < 0 ? "FALSE" : Integer.toString(api.settings.getTimeRemaining())), x, y + 50);
    }

    @Override
    public int onLoop() throws InterruptedException {

        getNextTrap();

        if (isSettingTrap()) {
            hoverNextAction();
        } else if (!isSettingTrap() && !api.myPlayer().isMoving() && nextTrap != null) {
            status = States.SETTING;
            resetTrap();
        }
        else if(!menu.isOpen())
        {
            MethodProvider.sleep((long)api.getReactionAfk());
            status = States.IDLE;
        }

        return random(100, 150);
    }

    private void resetTrap() throws InterruptedException {

        String option;

        if(nextTrap.getId() == boxTrap)
            option = "Lay";
        else
            option = "Reset";

        if(menu.isOpen()) {
            MethodProvider.sleep((long)api.getReactionPredict());
            if(mouse.click(false)) {
                for(int i = 0; i < 100 && !isSettingTrap(); i++) {
                    MethodProvider.sleep(random(20, 40));
                }
                if(isSettingTrap())
                    currentTrap = nextTrap;
            }
        } else {
            if(nextTrap != null && nextTrap.isVisible()) {
                if(nextTrap.interact(option)) {
                    for(int i = 0; i < 100 && !isSettingTrap(); i++) {
                        MethodProvider.sleep(random(20, 40));
                    }
                    if(isSettingTrap())
                        currentTrap = nextTrap;
                }
            } else if(nextTrap != null && !nextTrap.isVisible()) {
                camera.toEntity(nextTrap);
            } else {
                log("No Traps");
            }
        }
    }

    private void getNextTrap() {

        if(nextTrap != null && currentTrap != null)
            if(nextTrap == currentTrap)
                nextTrap = null;


        if(nextTrap != null && nextTrap.exists())
            return;

        nextTrap = null;

        nextTrap = api.getGroundItems().closest(obj -> trapLocations.contains(obj.getPosition())
                && obj.getId() == boxTrap && obj != currentTrap && !api.myPlayer().getArea(0).contains(obj));

        if(nextTrap == null)
        {
            nextTrap = api.getObjects().closest(obj -> trapLocations.contains(obj.getPosition()) && obj.getId() == shakingTrap && obj != currentTrap);
        }

        if (nextTrap == null)
        {
            nextTrap = api.getObjects().closest(obj -> trapLocations.contains(obj.getPosition())
                    && obj.getId() == emptyTrap && obj != currentTrap);
        }
    }

    private boolean isSettingTrap() {
        boolean isSet = api.getObjects().closest(o -> api.myPlayer().getArea(0).contains(o) && o.getId() == setTrap) != null;

        return (isResetingTrap || api.myPlayer().isAnimating()) && !isSet;
    }

    private void hoverNextAction() throws InterruptedException {

        if(random(0,119) > 98)
        {
            MethodProvider.sleep((long)api.getReactionNormal());
            return;
        }

        if (nextTrap != null) {
            if(nextTrap.getId() == boxTrap)
                hoverEntityOption(nextTrap,"Lay");
            else
                hoverEntityOption(nextTrap,"Reset");
        }

    }

    private boolean hoverEntityOption(Entity entity, String option) throws InterruptedException {
        if (entity == null)
            return false;

        if (menu.isOpen()) {
            List<Option> options = api.menu.getMenu();

            if (options != null) {
                Rectangle optionRec;

                for (int index = 0; index < options.size(); index++) {
                    if (options.get(index).action.equals(option)) {

                        optionRec = menu.getOptionRectangle(index);

                        if (optionRec != null) {
                            if (!optionRec.contains(mouse.getPosition())) {
                                MethodProvider.sleep((long)api.getReactionPredict());
                                return mouse.move(new RectangleDestination(api.getBot(),optionRec));
                            }
                        }
                    }
                }
            }
        } else {
            boolean correctMenu = false;
            MethodProvider.sleep((long)api.getReactionPredict());
            EntityDestination ed = new EntityDestination(bot, entity);
            mouse.click(ed, true);

            if(menu.isOpen())
            {
                List<Option> options = api.menu.getMenu();

                for (int index = 0; index < options.size(); index++) {
                    if (options.get(index).action.equals(option)) {
                        correctMenu = true;
                    }
                }

                if(!correctMenu)
                {
                    MethodProvider.sleep((long)api.getReactionPredict());
                    ed = new EntityDestination(bot, entity);
                    mouse.click(ed, true);
                }
            }
        }

        return false;
    }

    public enum States {
        INIT,
        SETTING,
        IDLE
    }

}
