package hunter.nodes;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;
import util.DefaultAPI;
import util.Node;

import java.awt.event.KeyEvent;

public class DropNode extends Node {

    public DropNode(DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public NodeType getType() {
        return NodeType.DROP;
    }

    @Override
    public boolean validate() {
        return api.getInventory().isFull();
    }

    @Override
    public void execute() throws InterruptedException {
        fishSleep();
        dropAll();
    }

    private void fishSleep () throws InterruptedException
    {
        MethodProvider.sleep((long) api.getReactionAfk());
    }

    private void dropAll() throws InterruptedException
    {
        int dropSequence[] = dropSequence();

        api.getKeyboard().pressKey(KeyEvent.VK_SHIFT);
        MethodProvider.sleep(MethodProvider.random(20,30));

        for (int i : dropSequence)
        {
            Item item = api.getInventory().getItemInSlot(i);
            if(item != null)
            {
                api.getInventory().interact(i);
                MethodProvider.sleep(MethodProvider.random(21,32));
            }
        }

        api.getKeyboard().releaseKey(KeyEvent.VK_SHIFT);
    }

    private int[] dropSequence()
    {
        int random = MethodProvider.random(0,111);

        if(random < 83)
        {
            if (random < 18)
                return (new int[] {0,4,1,5,2,6,3,7,8,12,9,13,10,14,11,15,16,20,17,21,18,22,19,23,24,25});

            return (new int[] {0,4,1,5,2,6,3,7,8,12,9,13,10,14,11,15,16,20,17,21,18,22,19,23,25,24});
        }
        else
        {
            return (new int[] {0,1,4,5,8,9,12,13,16,17,20,21,24,25,2,3,6,7,10,11,14,15,18,19,22,23});
        }
    }

}
