package combat;

import UI.AIOGUI;
import extra.*;
import mouse.MouseCursor;
import mouse.MousePathPoint;
import mouse.MouseTrail;
import nodes.AttackNode;
import nodes.IdleNode;
import nodes.InventoryNode;
import nodes.LootNode;
import org.osbot.rs07.api.filter.AreaFilter;
import org.osbot.rs07.api.filter.ContainsNameFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.GraphicUtilities;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//if "in combat" but character is not attacking after 2 seconds : find new targer
//if item on ground only loot before finding a target



@ScriptManifest(version = 0.1, author = "removed" ,  name = "removedCombat", logo = "", info = "AIOCombat")
public class removedCombat extends Script {

    private Thread monitor;
    private String status;
    private DefaultAPI api;

    private List<Node> nodes = new ArrayList<>();

    private Long startTime;

    // mouse paint
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.white, this);

    @Override
    public final void onStart() throws InterruptedException {
        api = new DefaultAPI(this.bot, new Settings(), new Extra(), new ReactionGenerator());

        launchGUI();

        setupThreads();

        initialise();

        if (api.getExtraSettings().getRemainingKills() < 0) {
            MethodProvider.sleep((long) api.getReactionPredict());

            if (api.getEquipment().contains(new ContainsNameFilter<>("Slayer helmet"))) {
                api.getEquipment().interact(EquipmentSlot.HAT, "Check");

                Sleep.sleepUntil(() -> api.getExtraSettings().getRemainingKills() != -1, random(1000, 2000));
            }
        }
    }

    @Override
    public void onPaint(final Graphics2D g) {
        if (api == null || status == null || startTime == null)
            return;

        int x = 15;
        int y = 270;

        Font font = new Font("Open Sans", Font.BOLD, 10);
        g.setFont(font);
        g.setColor(Color.yellow);
        g.drawString("Run time: " + (api.getExtra().formatTime(System.currentTimeMillis() - startTime)), x, y);
        g.drawString("Status: " + status, x, y + 10);
        g.drawString("Kills left: " + api.getExtraSettings().getRemainingKills().toString(), x, y + 20);

        if (api.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK) != 0)
            g.drawString("Attack xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.ATTACK) + " TTL: " + api.getExtra().formatTime(api.getExperienceTracker().getTimeToLevel(Skill.ATTACK)), x, y + 30);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH) != 0)
            g.drawString("Strength xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.STRENGTH) + " TTL: " + api.getExtra().formatTime(api.getExperienceTracker().getTimeToLevel(Skill.STRENGTH)), x, y + 40);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.DEFENCE) != 0)
            g.drawString("Defence xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.DEFENCE) + " TTL: " + api.getExtra().formatTime(api.getExperienceTracker().getTimeToLevel(Skill.DEFENCE)), x, y + 50);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.SLAYER) != 0)
            g.drawString("Slayer xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.SLAYER) + " TTL: " + api.getExtra().formatTime(api.getExperienceTracker().getTimeToLevel(Skill.SLAYER)), x, y + 60);

        if (api.getMouse().isOnScreen()) {
            trail.paint(g);
            cursor.paint(g);
        }

        drawNpcs(g);
    }

    @Override
    public final void onMessage(final Message message) {

        if (message.getType() == Message.MessageType.GAME) {

            if (message.getMessage().contains("Valuable drop:")) {
                String dropMessage = message.getMessage();

                Matcher m = Pattern.compile(": (.+) \\(").matcher(dropMessage);
                if (m.find()) {
                    String lootName = m.group(1).replaceAll("\\d", "").replaceAll(" x ", "").replaceAll(",", "");

                    switch (lootName) {
                        case "Rune h sword":
                            lootName = "Rune 2h sword";
                            break;

                        default:
                            break;
                    }

                    api.log(lootName);

                    GroundItem loot = api.getGroundItems().closest(lootName);

                    if (loot != null) {
                        api.getExtraSettings().addLootItem(lootName);
                    }
                }
            } else if (message.getMessage().contains("Untradeable drop")) {
                String dropMessage = message.getMessage();

                String lootName = dropMessage.replace("Untradeable drop: ", "").replace("<col=ef1020>", "");

                api.log(lootName);

                GroundItem loot = api.getGroundItems().closest(lootName);

                if (loot != null) {
                    api.getExtraSettings().addLootItem(lootName);
                    log(loot);
                }

            } else if (message.getType() == Message.MessageType.GAME && message.getMessage().startsWith("You're assigned to kill")) {
                api.getExtraSettings().setRemainingKills(Integer.parseInt(message.getMessage().replaceAll("[\\D]", "")));
            } else if (message.getType() == Message.MessageType.GAME && message.getMessage().contains("return to a Slayer master")) {
                stop(false);
            }
        }
    }


    @Override
    public int onLoop() throws InterruptedException {
        if (status == null)
            return 2000;

        for (Node node : nodes) {
            if (node.validate()) {
                status = node.getType().toString();
                node.execute();
                api.getExtraSettings().setActiveNode(node.getType());
                break;
            }
        }
        return random(250, 500);
    }

    @Override
    public final void onExit() {
        monitor.interrupt();
    }

    private void setupThreads() {
        Monitor m = new Monitor(api);
        monitor = new Thread(m);
        monitor.start();
    }

    private void launchGUI() throws InterruptedException {
        AIOGUI gui = new AIOGUI(api);
        gui.main(null);

        while (!gui.getSettingsSaved()) {
            sleep(500);
        }
    }

    private void initialise() {
        api.getExtraSettings().setFightArea(myPlayer().getArea(api.getExtraSettings().getFightRadius()));

        status = "Init";
        startTime = System.currentTimeMillis();
        nodes.add(new AttackNode(api));
        nodes.add(new InventoryNode(api));
        nodes.add(new IdleNode(api));
        nodes.add(new LootNode(api));
        nodes.sort(Comparator.comparingInt(Node::getPriority));

        api.getExperienceTracker().start(Skill.ATTACK);
        api.getExperienceTracker().start(Skill.STRENGTH);
        api.getExperienceTracker().start(Skill.DEFENCE);
        api.getExperienceTracker().start(Skill.SLAYER);
    }

    private void drawNpcs(Graphics2D g) {

        NPC nextNPC = api.getNextMob(false);

        if (nextNPC != null && nextNPC.isOnScreen()) {
            //g.draw(GraphicUtilities.getModelArea(api.getBot(), nextNPC.getGridX(), nextNPC.getGridY(), nextNPC.getZ(), nextNPC.getModel()));
            //g.fill(GraphicUtilities.getModelArea(getBot(), nextNPC.getGridX(), nextNPC.getGridY(), nextNPC.getZ(), nextNPC.getModel()));
        }
    }
}
