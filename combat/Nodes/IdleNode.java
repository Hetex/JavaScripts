package nodes;
import extra.DefaultAPI;
import extra.Node;
import extra.NodeType;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

import static org.osbot.rs07.script.MethodProvider.random;
import static org.osbot.rs07.script.MethodProvider.sleep;

public class IdleNode extends Node {

    private int specialPercentage = 50;

    public IdleNode(DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public NodeType getType() {
        return NodeType.IDLE;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void execute() throws InterruptedException {

        if(api.getSettings().getRunEnergy() > 30 && !api.getSettings().isRunning())
        {
            sleep((long)api.getReactionAfk());

            api.getSettings().setRunning(true);
        }
        else if(api.getExtraSettings().isUsePrayer() && api.getSkills().getDynamic(Skill.PRAYER) > 0 && !api.getPrayer().isQuickPrayerActive())
        {
            sleep((long)api.getReactionNormal());

            RS2Widget quickPrayer = api.getWidgets().get(160,14);

            if(quickPrayer != null)
            {
                quickPrayer.interact("Activate");

                sleep(random(100,250));
            }
        }
        else if(api.getInventory().contains("Vial"))
        {
            api.getReactionPredict();

            api.getInventory().getItem("Vial").interact("Drop");

            MethodProvider.sleep(random(300,600));
        }
        else if(api.getExtraSettings().isUseSpecial() && api.getExtraSettings().isInCombat() && api.getCombat().getSpecialPercentage() >= specialPercentage && !api.getCombat().isSpecialActivated())
        {
            api.getReactionAfk();

            api.getCombat().toggleSpecialAttack(true);

            specialPercentage = random(50,90);

            sleep(random(200,500));
        }
        else if(api.getExtraSettings().isInCombat())
        {

            if(true)
                return;

            api.getReactionAfk();

            NPC combatNPC = (NPC)api.myPlayer().getInteracting();

            /*if(combatNPC != null)
            {
                if(api.myPlayer().getInteracting().getHealthPercent() < 30)
                {
                    if(api.getExtraSettings().getLootItems().size() > 0)
                    {
                        String lootItem = api.getExtraSettings().getLootItems().get(0);

                        if(lootItem != null)
                        {
                            if(!lootItem.isOnScreen())
                            {
                                api.getCamera().toEntity(lootItem);
                            }
                            else
                            {
                                if(!lootItem.getModel().getBoundingBox(lootItem.getGridX(),lootItem.getGridY(),lootItem.getZ()).contains(api.getMouse().getPosition()))
                                {
                                    //lootItem.hover();
                                }
                            }
                        }
                    }
                    else
                    {
                        NPC nextmob = api.getNpcs().closest(npc ->
                                Arrays.asList(api.getExtraSettings().getNpcs()).contains(npc.getName())
                                        && npc.getHealthPercent() != 0);

                        if(nextmob != null)
                        {
                            if(!nextmob.isOnScreen())
                            {
                                api.getCamera().toEntity(nextmob);
                            }
                        }
                    }
                }
            }*/
        }
    }
}
