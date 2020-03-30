package hunter.nodes;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import util.DefaultAPI;
import util.Node;
import util.Sleep;

public class FishNode extends Node {

    public FishNode(DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public NodeType getType() {
        return NodeType.FISH;
    }

    @Override
    public boolean validate() {
        return !api.myPlayer().isAnimating();
    }

    @Override
    public void execute() throws InterruptedException {
        fishSleep();

        NPC fishingSpot = api.npcs.closest(true, n -> n.getName().equals("Fishing spot"));

        if(fishingSpot == null) {
            fishingSpot = api.npcs.closest("Fishing spot");
            if (fishingSpot == null)
                return;
        }

        if(api.myPlayer().isAnimating())
            return;

        if(fishingSpot.interact("Use-rod"))
        {
            if(MethodProvider.random(0,92) > 19)
                api.getMouse().moveOutsideScreen();

            Sleep.sleepUntil(() -> api.myPlayer().isAnimating(), 5000);
        }
    }

    private void fishSleep () throws InterruptedException
    {
        if(api.settings.getLastActive() == NodeType.DROP)
        {
            MethodProvider.sleep((long) api.getReactionPredict());
        }
        else
        {
            MethodProvider.sleep((long) api.getReactionAfk());
        }
    }
}
