package hunter.nodes;

import org.osbot.rs07.script.MethodProvider;
import util.DefaultAPI;
import util.Node;

public class IdleNode extends Node {

    public IdleNode (DefaultAPI api) {
        super(api);
    }

    @Override
    public int getPriority() {
        return 10;
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
        MethodProvider.sleep(MethodProvider.random(1000,2000));
    }
}
