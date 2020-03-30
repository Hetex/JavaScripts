import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import util.Mode;
import util.SkillTracker;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.rspeer.runetek.api.commons.math.Random.nextInt;

@ScriptMeta(developer = "Sane", desc = "combat", name = "combat")
public class combat extends Script implements RenderListener, ChatMessageListener {

    Extra ex = new Extra();
    SkillTracker st = new SkillTracker();

    private long startTime;

    private Mode mode = Mode.ATTACK;

    static String mobName;
    static boolean specialAttack;
    static boolean usePotions;

    private ArrayList<String> loot = new ArrayList<>();
    private int lootCount = 0;

    private boolean eatLow = false;
    private int eatPercentage = 45;

    private boolean potUsed = false;
    private int levelRange = 5;

    @Override
    public void onStart() {

        Log.info("STARTING");

        new combatGUI().setVisible(true);

        startTime = System.currentTimeMillis();

        st.track(Skill.ATTACK);
        st.track(Skill.STRENGTH);
        st.track(Skill.DEFENCE);

        startTime = System.currentTimeMillis();
    }

    @Override
    public void notify(RenderEvent e) {

        int xpGained = st.getGainedXP(Skill.ATTACK) + st.getGainedXP(Skill.STRENGTH) + st.getGainedXP(Skill.DEFENCE);
        int xpHour = st.getGainedXPPerHour(Skill.ATTACK) + st.getGainedXPPerHour(Skill.STRENGTH) + st.getGainedXPPerHour(Skill.DEFENCE);

        String ttl = "";

        if (st.getGainedXP(Skill.ATTACK) != 0) {
            ttl = ex.formatTime(st.getTimeToLevel(Skill.ATTACK));
        } else if (st.getGainedXP(Skill.STRENGTH) != 0) {
            ttl = ex.formatTime(st.getTimeToLevel(Skill.STRENGTH));
        } else if (st.getGainedXP(Skill.DEFENCE) != 0) {
            ttl = ex.formatTime(st.getTimeToLevel(Skill.DEFENCE));
        }

        final long upTime = System.currentTimeMillis() - startTime;

        Graphics g = e.getSource();
        Graphics2D g2 = (Graphics2D) g;

        int y = 210;
        int x = 10;

        g2.setColor(Color.CYAN);
        g2.drawString("Status: " + mode, x, y += 20);
        g2.drawString("Runtime: " + ex.formatTime(upTime), x, y += 20);
        g2.drawString("Experience gained: " + xpGained, x, y += 20);
        g2.drawString("Exp/hr: " + xpHour, x, y += 20);
        g2.drawString("TTL: " + ttl, x, y += 20);

    }


    @Override
    public int loop() {

        if (mobName == null)
            return 500;

        Player me = Players.getLocal();

        if(Inventory.getFirst("Monkfish") == null)
            setStopping(true);

        if (me.getHealthPercent() < eatHp()) {
            eatLow = true;
            eat(true);
        } else if (!ex.inCombat()) {

            if (me.getHealthPercent() < 70 && Skills.getCurrentLevel(Skill.HITPOINTS) + 16 < Skills.getLevel(Skill.HITPOINTS))
                eat(false);
            else if(lootCount > 0 && Inventory.getFreeSlots() > 0)
            {
                loot();
            }
            else if(usePotions && Skills.getLevel(Skill.DEFENCE) + potLevel() > Skills.getCurrentLevel(Skill.DEFENCE))
            {
                potUp();
            }
            else
                attack();

        } else if (specialAttack && (Varps.get(300) / 10) > 55) {
            toggleSpecial();
        } else {
            mode = Mode.AFK;
        }
        return nextInt(100, 200);
    }

    private void attack() {

        Npc mob = Npcs.getNearest(x -> x.getName().equals(mobName) && x.getTargetIndex() == -1 && x.getHealthPercent() > 0 && x.isPositionInteractable());

        if (mob != null) {

            Npc close = Npcs.getNearest(x -> x.getName().equals(mobName) && x.getTargetIndex() == -1 && x.getHealthPercent() > 0);

            if(close != null)
                mob = close;

            Time.sleep(sleep(false));

            if(lootCount > 0)
                return;

            Log.info("ATTACKING: " + mob.getName());

            mob.interact("Attack");

            Time.sleepUntil(() -> ex.inCombat(), Random.nextInt(2000, 3000));
        }

        mode = Mode.ATTACK;
    }

    private void toggleSpecial()
    {
        Time.sleep(sleep(false));

        Combat.toggleSpecial(true);
    }

    private void eat(boolean urgent)
    {
        if(!Tabs.isOpen(Tab.INVENTORY))
        {
            Time.sleep(sleep(false));
            Tabs.open(Tab.INVENTORY);
        }

        Player me = Players.getLocal();

        int currentHp = me.getHealthPercent();

        Item food = Inventory.getFirst("Monkfish");

        if(food != null)
        {
            Time.sleep(sleep(urgent));
            food.interact("Eat");
            Log.info("EATING: " + food.getName());
            Time.sleepUntil(() -> me.getHealthPercent() > currentHp, Random.nextInt(1000,2000));
        }

        mode = Mode.EAT;
    }

    private void potUp() {
        if(!Tabs.isOpen(Tab.INVENTORY))
        {
            Time.sleep(sleep(false));
            Tabs.open(Tab.INVENTORY);
        }

        Player me = Players.getLocal();

        int attackLevel = Skills.getCurrentLevel(Skill.ATTACK);
        int strengthLevel = Skills.getCurrentLevel(Skill.STRENGTH);
        int defenceLevel = Skills.getCurrentLevel(Skill.DEFENCE);

        Item attackPot = Inventory.getItems(x -> x.getName().contains("Super attack"))[0];
        Item strengthPot = Inventory.getItems(x -> x.getName().contains("Super strength"))[0];
        Item defencePot = Inventory.getItems(x -> x.getName().contains("Super defence"))[0];

        if (Skills.getLevel(Skill.ATTACK) + potLevel() > attackLevel) {
            if (attackPot != null) {
                Time.sleep(sleep(false));
                attackPot.interact("Drink");
                Log.info("DRINKING: " + attackPot.getName());
                Time.sleepUntil(() -> Skills.getLevel(Skill.ATTACK) + 5 < attackLevel, nextInt(1500, 2300));
            }
        } else if (Skills.getLevel(Skill.STRENGTH) + potLevel() > strengthLevel) {
            if(strengthPot != null) {
                Time.sleep(sleep(false));
                strengthPot.interact("Drink");
                Log.info("DRINKING: " + strengthPot.getName());
                Time.sleepUntil(() -> Skills.getLevel(Skill.STRENGTH) + 5 < strengthLevel, nextInt(1500, 2300));
            }
        } else if (Skills.getLevel(Skill.DEFENCE) + potLevel() > defenceLevel) {
            if(defencePot != null) {
                Time.sleep(sleep(false));
                defencePot.interact("Drink");
                Log.info("DRINKING: " + defencePot.getName());
                potUsed = true;
                Time.sleepUntil(() -> Skills.getLevel(Skill.STRENGTH) + 5 < strengthLevel, nextInt(1500, 2300));
            }
        }
        mode = Mode.EAT;
    }

    private void loot()
    {
        int freeSlots = Inventory.getFreeSlots();

        Pickable groundLoot = Pickables.getNearest(x -> loot.contains(x.getName()));

        if(groundLoot != null)
        {
            Time.sleep(sleep(false));
            groundLoot.interact("Take");
            Time.sleepUntil(() -> Inventory.getFreeSlots() < freeSlots, nextInt(1500,2000));
            lootCount--;
        }
    }

    private int sleep(boolean urgent)
    {
        int count = (int)ex.getReactionNormal();

        if(urgent)
            return count;

        int randomInteger = nextInt(0,11);

        if(randomInteger < 3 && mode == Mode.AFK)
            return (int)ex.getReactionAfk();
        else if(randomInteger < 9)
            return (int)ex.getAttackAfk();

        return count;
    }

    private int eatHp()
    {
        if(eatLow)
            eatPercentage = nextInt(28,69);

        eatLow = false;

        return eatPercentage;
    }

    private int potLevel()
    {
        if(potUsed)
            levelRange = nextInt(3,7);

        potUsed = false;

        return  levelRange;
    }

    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        if (chatMessageEvent.getType() == ChatMessageType.SERVER) {

            if(chatMessageEvent.getMessage().contains("completed"))
            {
                setStopping(true);
            }

            if (chatMessageEvent.getMessage().contains("Valuable drop:")) {
                String dropMessage = chatMessageEvent.getMessage();

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

                    Log.info(lootName + " detected");
                    lootCount++;
                    loot.add(lootName);
                }
            }else if (chatMessageEvent.getMessage().contains("Untradeable drop")) {
                String dropMessage = chatMessageEvent.getMessage();

                String lootName = dropMessage.replace("Untradeable drop: ", "").replace("<col=ef1020>", "");

                lootCount++;
                loot.add(lootName);
            }
        }
    }
}

