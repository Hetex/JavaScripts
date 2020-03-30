package extra;

import org.osbot.rs07.api.model.NPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Monitor implements Runnable {

    private DefaultAPI api;
    private List<NPC> inCombatList= new ArrayList<>();
    private int counter = 0;

    public Monitor(DefaultAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted())
                return;

            if(api.getNpcs().getAll().stream().anyMatch(npc -> npc.isInteracting(api.myPlayer()) && npc.getHealthPercent() == 0 && npc.hasAction("Attack") && npc.exists()) && api.getExtraSettings().isInCombat())
            {
                if(api.getNpcs().getAll().stream().noneMatch(npc -> npc.hasAction("Attack")
                        && npc.isInteracting(api.myPlayer())
                        && npc.getHealthPercent() != 0
                        && api.myPlayer().getArea(4).contains(npc.getX(),npc.getY())))
                {
                    api.getExtraSettings().setInCombat(false);
                }
            }
            else if(counter > 15)
            {
                api.log("Focus Counter++");
                api.getExtraSettings().setInCombat(false);
                if(api.getCombat().isFighting())
                {
                    counter = 0;
                }
            }
            else if (api.getNpcs().getAll().stream().anyMatch(
                    npc -> npc.hasAction("Attack")
                    && npc.getHealthPercent() != 0
                    && (api.myPlayer().isInteracting(npc) || (npc.isInteracting(api.myPlayer()) && api.myPlayer().getArea(3).contains(npc.getX(),npc.getY()))
                    && Arrays.asList(api.getExtraSettings().getNpcs()).contains(npc.getName()))))
            {
                api.getExtraSettings().setInCombat(true);
                NPC monster = (NPC)api.myPlayer().getInteracting();
                if(monster != null && monster.getHealthPercent() != 0)
                {
                    if(!inCombatList.contains(monster))
                    {
                        inCombatList.add(monster);
                    }
                }
                if(!api.getCombat().isFighting()) {
                    counter++;
                }
                else
                {
                    counter = 0;
                }
            }
            else
            {
                api.getExtraSettings().setInCombat(false);
            }

            if(inCombatList.size() > 0)
            {
                Iterator<NPC> it = inCombatList.iterator();

                while(it.hasNext())
                {
                    NPC npc = it.next();

                    if(!npc.exists())
                    {
                        api.getExtraSettings().setRemainingKills(api.getExtraSettings().getRemainingKills() - 1);
                        it.remove();
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                api.log("Catch Interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
