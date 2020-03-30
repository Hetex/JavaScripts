import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.Collections;

@ScriptManifest(author = "removed", name = "NMZ", info = "Simple NMZ", version = 0.1, logo = "")
public final class removedNMZ extends Script
{
    private final Area NMZ_AREA = new Area(2600, 3119, 2609, 3112);
    private final Area dreamArea = new Area(10912, 1816, 10943, 1847);
    private Area fightArea;
    private Position initialPos;

    private final int dreamSellerID = 1120;
    private final int potionID = 26291;

    private final int absorptionBarrelId = 26280;
    private final int overloadBarrelId = 26279;
    private final int absorptionPotion4Id = 11734;
    private final int overloadPotion4Id = 11730;
    private final int rockcakeId = 7510;

    private final int[] absorptionIds = {11734,11735,11736,11737};
    private final int[] overloadIds = {11730,11731,11732,11733};

    private final int numberOfAbsorption4 = 18;
    private final int numberOfOverload4 = 9;

    private State state;
    private DreamState dreamState;

    private int counter = 0;

    private enum State
    {
        INIT,

        INITDREAM,

        DREAM,

        IDLE,

        WAIT
    }

    private enum DreamState
    {
        INIT,

        MAINTENANCE,

        IDLE
    }

    @Override
    public final void onStart() {
        log("Welcome to NMZ");

        if(inDream()){
            state = State.DREAM;
            dreamState = dreamState.MAINTENANCE;
        }
        else {
            state = State.INIT;
        }


    }

    @Override
    public final int onLoop() throws InterruptedException
    {
        switch(state){
            case INIT:
                init();
                break;

            case INITDREAM:
                initDream();
                break;

            case DREAM:
                dreamHandler();
                break;

            default:
                break;
        }

        return random(500,2000);
    }

    @Override
    public void onPaint(final Graphics2D g) {
        if(state == null)
            return;

        g.drawString("State: " + state.toString() + ", Dream state: " + dreamState.toString(), 10, 10);
    }

    @Override
    public final void onExit() {

    }

    private void init() throws InterruptedException
    {
        if(!inArea(NMZ_AREA)) {
            if (getWalking().walk(NMZ_AREA)) {
                Sleep.sleepUntil(() -> !myPlayer().isMoving(), 10000);
            }

        }
        else if(!isInventorySetup())
        {
            initInventory();
        }
        else if(isInventorySetup())
        {
            initialPos = null;
            state = State.INITDREAM;
        }
    }

    private boolean inArea(Area area) {
        return area.contains(myPlayer().getX(), myPlayer().getY());
    }

    private boolean isInventorySetup()
    {
        if(getInventory().getAmount(absorptionPotion4Id) != numberOfAbsorption4
                || getInventory().getAmount(overloadPotion4Id) != numberOfOverload4
                || getInventory().getAmount(rockcakeId) != 1)
        {
            return false;
        }

        return true;
    }

    private void initInventory() throws InterruptedException
    {
        long currentAbsorptions = getInventory().getAmount(absorptionPotion4Id);
        long currentOverloads = getInventory().getAmount(overloadPotion4Id);

        long withdrawAmountAbsorptions = (numberOfAbsorption4 - currentAbsorptions) * 4;
        long withdrawAmountOverLoad = (numberOfOverload4 - currentOverloads) * 4;

        if(withdrawAmountOverLoad > 0 )
        {
            takePotions(withdrawAmountOverLoad, overloadBarrelId);
        }
        else if(withdrawAmountAbsorptions > 0)
        {
            takePotions(withdrawAmountAbsorptions, absorptionBarrelId);
        }
    }

    //Missing logic for reset potions
    private void takePotions(long number, int barrel)
    {
        RS2Object barrelObject = getObjects().closest(barrel);

        if(barrelObject != null)
        {
            if(barrelObject.isVisible())
            {
                RS2Widget withdrawX = getWidgets().get(162,37);

                if(withdrawX != null && withdrawX.isVisible())
                {
                    getKeyboard().typeString(Long.toString(number), true);
                }
                else if(!myPlayer().isMoving())
                {
                    barrelObject.interact("Take");

                    Sleep.sleepUntil(() -> !myPlayer().isMoving(), random(2000,3000));
                }
            }
            else
            {
                camera.toEntity(barrelObject);
            }
        }
        else
        {
            log("No barrel detected");
            stop();
        }
    }

    private void initDream()
    {
        NPC dreamSellerNpc = getNpcs().closest(dreamSellerID);
        RS2Object potionObject = getObjects().closest(potionID);
        RS2Widget confirmDream = getWidgets().get(129,6,0);

        if(confirmDream != null && confirmDream.isVisible()) {

            log("Confirming dream");

            if(confirmDream.interact("Continue"))
            {
                Sleep.sleepUntil(() -> inDream(), random(5000, 7500));

                if (inDream()) {
                    state = State.DREAM;
                    dreamState = DreamState.INIT;
                }
                else
                {
                    stop();
                    log("Error entering dream");
                }
            }
        }
        else
        {
            if(potionObject != null && potionObject.isVisible() && potionObject.hasAction("Drink"))
            {
                potionObject.interact("Drink");
            }
            else if(dreamSellerNpc != null)
            {
                if(!dreamSellerNpc.isVisible())
                {
                    camera.toEntity(dreamSellerNpc);
                }
                else if(dialogues.inDialogue())
                {
                    if(dialogues.isPendingContinuation())
                    {
                        dialogues.clickContinue();
                    }
                    else if(dialogues.isPendingOption())
                    {
                        dialogues.selectOption("Yes");
                        dialogues.selectOption(4);
                    }
                }
                else if(dreamSellerNpc.interact("Dream") && !myPlayer().isMoving())
                {
                    Sleep.sleepUntil(() -> dialogues.inDialogue(), random(2000, 4000));
                }
            }
        }
    }

    private void dreamHandler() throws InterruptedException {
        if (inDream())
        {
            switch(dreamState)
            {
                case INIT:
                    dreamPrep();
                    break;
                case MAINTENANCE:
                    dreamMaintenance();
                    break;
                case IDLE:
                    dreamIdler();
                    break;
            }
        }
        else
        {
            state = State.INIT;
        }
    }



    private int getCurrentDrinkLevel() {
        RS2Widget widget = getWidgets().get(202, 1, 9);
        if(widget != null && widget.isVisible() && widget.getMessage() != null)
            return Integer.parseInt(widget.getMessage().replace(",", ""));
        return 0;
    }

    private int getCurrentPoints()
    {
        RS2Widget widget = getWidgets().get(202,1,3);
        if(widget != null && widget.isVisible() && widget.getMessage() != null)
            return  Integer.parseInt(widget.getMessage().replace("Points:<br>", ""));
        return -1;
    }

    private void drinkPotion(int[] ids, Integer number) throws InterruptedException
    {
        if (getInventory().contains(ids)) {

            if(number == null)
            {
                getInventory().getItem(ids).interact("Drink");
            }
            else if(number < 0)
            {
                while(getCurrentDrinkLevel() < 1000)
                {
                    if(!getInventory().getItem(ids).interact("Drink"))
                    {
                        log("Fail to drink absorb");
                        break;
                    }
                    sleep(random(160,300));
                }
            }
            else
            {
                for(int i = 0; i < number ; i++)
                {
                    if(!getInventory().getItem(ids).interact("Drink"))
                    {
                        log("Fail to drink overload");
                        break;
                    }
                    sleep(random(160,300));
                }
            }
        }
    }

    private boolean inDream()
    {
        return getCurrentPoints() != -1 ? true : false;
    }

    private void dreamPrep() throws InterruptedException
    {
        if(fightArea == null)
        {
            initialPos = myPlayer().getPosition();
            fightArea = new Area(initialPos.getX() - 12, initialPos.getY() + 12,
                    initialPos.getX(), initialPos.getY() + 25);
        }

        if(!myPlayer().isMoving() && !inArea(fightArea))
        {
            getWalking().walk(fightArea);
        }
        else
        {
            drinkPotion(absorptionIds, -1);

            drinkPotion(overloadIds, 1);

            getMouse().moveOutsideScreen();
            dreamState = DreamState.IDLE;
        }
    }

    private void dreamIdler() throws InterruptedException
    {
        if(getCurrentDrinkLevel() < 200 ||
                getSkills().getDynamic(Skill.ATTACK) == getSkills().getStatic(Skill.ATTACK))
        {
            if(getInventory().contains(absorptionIds) || getInventory().contains(absorptionIds))
            {
                dreamState = DreamState.MAINTENANCE;
            }
        }
    }

    private void dreamMaintenance() throws InterruptedException
    {
        if(getCurrentDrinkLevel() < 850 && getInventory().contains(absorptionIds))
        {
            drinkPotion(absorptionIds, -1);
        }
        else if (getSkills().getDynamic(Skill.ATTACK) == getSkills().getStatic(Skill.ATTACK)
                && getSkills().getDynamic(Skill.HITPOINTS) > 50 && getInventory().contains(overloadIds))
        {
            drinkPotion(overloadIds, 1);
        }
        else
        {
            dreamState = DreamState.IDLE;
        }
    }
}