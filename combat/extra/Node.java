package extra;
import org.osbot.rs07.Bot;

public abstract class Node{

    protected DefaultAPI api;

    public Node(DefaultAPI api) {
        this.api = api;
    }

    public abstract int getPriority();

    public abstract NodeType getType();

    public abstract boolean validate();

    public abstract void execute() throws InterruptedException;
}