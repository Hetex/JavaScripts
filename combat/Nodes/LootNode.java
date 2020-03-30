package nodes;

import extra.DefaultAPI;
import extra.Node;
import extra.NodeType;
import extra.Sleep;
import org.osbot.rs07.api.model.GroundItem;

import static org.osbot.rs07.script.MethodProvider.random;
import static org.osbot.rs07.script.MethodProvider.sleep;

public class LootNode extends Node {

    public LootNode(DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public NodeType getType() {
        return NodeType.LOOT;
    }

    @Override
    public boolean validate() {
        return (api.getExtraSettings().getLootItems().size() > 0 && !api.getExtraSettings().isInCombat());
    }

    @Override
    public void execute() throws InterruptedException {

        int inventorySpace = api.getInventory().getEmptySlotCount();

        switch (api.getExtraSettings().getActiveNode()) {
            case ATTACK:
                sleep((long)api.getReactionPredict());
                break;
            case INV:
            case IDLE:
            default:
                if (!api.getMouse().isOnScreen()) {
                    sleep((long) api.getReactionAfk());
                } else {
                    sleep((long) api.getReactionNormal());
                }
                break;

            case LOOT:
                sleep((long) api.getReactionPredict());
                break;
        }

        if (api.getExtraSettings().getLootItems().size() > 0) {

            String itemName = api.getExtraSettings().getLootItems().get(0);
            GroundItem item = api.getGroundItems().closest(itemName);

            if (item != null && item.exists() && api.getExtraSettings().getFightArea().contains(item.getX(),item.getY())) {
                if (api.getInventory().getEmptySlotCount() != 0 || (item.getDefinition().isNoted()) && api.getInventory().contains(item.getName())) {
                    if (item.isOnScreen()) {
                        if (item.interact("Take")) {
                            Sleep.sleepUntil(() -> api.getInventory().getEmptySlotCount() < inventorySpace, random(2000, 3000));
                        }
                    } else {
                        api.getCamera().toEntity(item);
                    }
                }
            } else {
                api.getExtraSettings().removeLootItem(itemName);
            }
        }
    }
}

