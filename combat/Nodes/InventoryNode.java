package nodes;
import extra.DefaultAPI;
import extra.Node;
import extra.NodeType;
import extra.Sleep;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

import static org.osbot.rs07.script.MethodProvider.random;

public class InventoryNode extends Node {

    private enum ReasonEnum {
        FOOD,
        POTION,
        PRAYER
    }

    private ReasonEnum reason;
    private int repotDifference = random(3,5);
    private int eatPercentage = random(40,60);
    private int prayerPercentage = random(40,60);
    private double prayerRestored;

    public InventoryNode(DefaultAPI api) {
        super(api);

        this.prayerRestored = Math.floor((api.getSkills().getStatic(Skill.PRAYER) / 4) + 7) + 2;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public NodeType getType() {
        return NodeType.INV;
    }

    @Override
    public boolean validate()
    {
        if(reason == null)
        {
            if (api.getExtraSettings().isUseFood() && api.myPlayer().getHealthPercent() < eatPercentage ) {
                reason = ReasonEnum.FOOD;
            }
            else if(api.getExtraSettings().isUsePrayer() && ((double)api.getSkills().getDynamic(Skill.PRAYER)/(double)api.getSkills().getStatic(Skill.PRAYER) * 100) < prayerPercentage)
            {
                reason = ReasonEnum.PRAYER;
            }
            else if (api.getExtraSettings().isUsePotions()
                    && api.getSkills().getDynamic(Skill.ATTACK) < api.getSkills().getStatic(Skill.ATTACK) + repotDifference
                    || api.getSkills().getDynamic(Skill.STRENGTH) < api.getSkills().getStatic(Skill.STRENGTH) + repotDifference
                    || (api.getSkills().getDynamic(Skill.DEFENCE) < api.getSkills().getStatic(Skill.DEFENCE) + repotDifference) && !api.getExtraSettings().isUsePrayer()) {
                reason = ReasonEnum.POTION;
            }
        }
        else switch(reason)
        {
            case FOOD:
                if(api.getSkills().getDynamic(Skill.HITPOINTS) + 20 > api.getSkills().getStatic(Skill.HITPOINTS))
                    reason = null;
                break;
            case PRAYER:
                if(api.getSkills().getDynamic(Skill.PRAYER) + prayerRestored > api.getSkills().getStatic(Skill.PRAYER))
                    reason = null;
                break;
            case POTION:
                if ((!api.getExtraSettings().isUsePotions()
                        || api.getSkills().getDynamic(Skill.ATTACK) >= api.getSkills().getStatic(Skill.ATTACK) + repotDifference)
                        && api.getSkills().getDynamic(Skill.STRENGTH) >= api.getSkills().getStatic(Skill.STRENGTH) + repotDifference
                        && ((api.getSkills().getDynamic(Skill.DEFENCE) >= api.getSkills().getStatic(Skill.DEFENCE) + repotDifference) || api.getExtraSettings().isUsePrayer())) {
                            reason = null;
                        }
                break;
        }

        return reason != null;
    }

    @Override
    public void execute() throws InterruptedException
    {
        MethodProvider.sleep(300);
        inventorySleep();

        if(api.getTabs().getOpen() != Tab.INVENTORY)
        {
            api.getTabs().open(Tab.INVENTORY);
            inventorySleep();
        }

        switch (reason) {
            case FOOD:
                eatFood();
                break;
            case PRAYER:
                prayerPotionHandler();
            case POTION:
                potionHandler();
                break;
        }
    }

    private void eatFood() {

        Item food = api.getInventory().getItem(f -> f.hasAction("Eat"));

        if (food != null) {
            if (food.interact("Eat")) {

                try{
                    MethodProvider.sleep((long)api.getReactionNormal());
                }
                catch(InterruptedException ignored){}

                eatPercentage = random(40,70);
            }
        }
    }

    private void potionHandler() {
        int[] attackIds = {2436, 145, 147, 149};
        int[] strengthIds = {2440, 157, 159, 161};
        int[] defenceIds = {2442, 163, 165, 167};

        if (api.getSkills().getDynamic(Skill.ATTACK) < api.getSkills().getStatic(Skill.ATTACK) + repotDifference + 1)
        {
            drinkPotion(attackIds, Skill.ATTACK);
        }
        else if (api.getSkills().getDynamic(Skill.STRENGTH) < api.getSkills().getStatic(Skill.STRENGTH) + repotDifference + 1) {
            drinkPotion(strengthIds, Skill.STRENGTH);
        }
        else if ((api.getSkills().getDynamic(Skill.DEFENCE) < api.getSkills().getStatic(Skill.DEFENCE) + repotDifference + 1) && !api.getExtraSettings().isUsePrayer()) {
            drinkPotion(defenceIds, Skill.DEFENCE);
        }
    }

    private void prayerPotionHandler()
    {
        int[] prayerIds =  {2434, 139, 141, 143};

        drinkPotion(prayerIds, Skill.PRAYER);

        prayerPercentage = random(20,60);
    }

    private void drinkPotion(int[] ids, Skill skill) {

        Item potion = api.getInventory().getItem(ids);

        if (potion != null) {

            try{
                MethodProvider.sleep((long)api.getReactionNormal());
            }
            catch(InterruptedException ignored){}

            potion.interact("Drink");
        }
    }

    private void inventorySleep() throws InterruptedException
    {
        MethodProvider.sleep((long)api.getReactionPredict());
    }
}
