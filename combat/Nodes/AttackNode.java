package nodes;
import extra.*;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

import static org.osbot.rs07.script.MethodProvider.random;

public class AttackNode extends Node {

    public AttackNode(DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public NodeType getType() {
        return NodeType.ATTACK;
    }

    @Override
    public boolean validate() {
        return !api.getExtraSettings().isInCombat() && api.getExtraSettings().getLootItems().size() != 1;
    }

    @Override
    public void execute() throws InterruptedException {

        NPC slayerTask = api.getNextMob(true);

        if (!api.getExtraSettings().isInCombat()) {
            if (slayerTask != null) {
                if (slayerTask.isOnScreen()) {

                    attackSleep();

                    if(api.getExtraSettings().getLootItems().size() > 0)
                        return;

                    if (slayerTask.interact("Attack")) {

                        if (api.getMouse().isOnScreen()) {
                            switch (random(0, 13)) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    api.getMouse().moveOutsideScreen();
                                    break;

                                default:
                                    break;
                            }
                        }

                        Sleep.sleepUntil(() -> api.getExtraSettings().isInCombat(), random(5000, 7000));
                    }
                } else {
                    attackSleep();

                    api.getCamera().toEntity(slayerTask);
                }
            }
        }
    }

    private void attackSleep() throws InterruptedException
    {
        switch(api.getExtraSettings().getActiveNode()) {
            case IDLE:
                int random = random(0, 14);

                if (random < 3) {
                    MethodProvider.sleep((long) api.getReactionAfk());
                    break;
                } else if (random < 10) {
                    MethodProvider.sleep((long) api.getReactionNormal());
                    break;
                }

            case INV:
            case LOOT:
                MethodProvider.sleep((long) api.getReactionPredict());
                break;

            default:
                break;
        }
    }
}