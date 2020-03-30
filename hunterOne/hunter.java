package hunter;

import hunter.nodes.DropNode;
import hunter.nodes.FishNode;
import hunter.nodes.IdleNode;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import util.DefaultAPI;
import util.Node;
import util.ReactionGenerator;
import util.Settings;
import util.mouse.MouseCursor;
import util.mouse.MouseTrail;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ScriptManifest(version = 0.1, author = "removed" ,  name = "removedFish", logo = "", info = "Fish")
public class removedHunter extends Script {

    //    private Thread monitor;
    private String status;
    private DefaultAPI api;//
    private List<Node> nodes = new ArrayList<>();

    private Long startTime;

    // mouse paint
    private MouseTrail trail = new MouseTrail(0, 255, 255, 2000, this);
    private MouseCursor cursor = new MouseCursor(25, 2, Color.white, this);

    @Override
    public final void onStart() throws InterruptedException {
        initialise();
    }

    @Override
    public void onPaint(final Graphics2D g) {
        if (api == null || status == null || startTime == null)
            return;

        if (api.getMouse().isOnScreen()) {
            trail.paint(g);
            cursor.paint(g);
        }

        int x = 15;
        int y = 270;

        Font font = new Font("Open Sans", Font.BOLD, 10);
        g.setFont(font);
        g.setColor(Color.yellow);
        g.drawString("Run time: " + (api.formatTime(System.currentTimeMillis() - startTime)), x, y);
        g.drawString("Status: " + status, x, y + 10);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.FISHING) != 0)
            g.drawString("Fishing xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.FISHING) + " TTL: " + api.formatTime(api.getExperienceTracker().getTimeToLevel(Skill.FISHING)), x, y + 20);
        if (api.getExperienceTracker().getGainedXPPerHour(Skill.AGILITY) != 0)
            g.drawString("Agility xp/h: " + api.getExperienceTracker().getGainedXPPerHour(Skill.AGILITY) + " TTL: " + api.formatTime(api.getExperienceTracker().getTimeToLevel(Skill.AGILITY)), x, y + 30);

        g.drawString("Sleeping for: " + (api.settings.getTimeRemaining() < 0 ? "FALSE" : Integer.toString(api.settings.getTimeRemaining())), x, y + 50);
    }

    @Override
    public final void onMessage(final Message message) {


    }


    @Override
    public int onLoop() throws InterruptedException {
        for (Node node : nodes) {
            if (node.validate()) {
                status = node.getType().toString();
                node.execute();
                api.settings.setLastActive(node.getType());
                break;
            }
        }
        return random(250, 500);
    }

    @Override
    public final void onExit() {
    }

    private void initialise()
    {
        api = new DefaultAPI(this.bot, new Settings(), new ReactionGenerator());
        status = "Init";
        startTime = System.currentTimeMillis();
        nodes.add(new FishNode(api));
        nodes.add(new DropNode(api));
        nodes.add(new IdleNode(api));
        nodes.sort(Comparator.comparingInt(Node::getPriority));

        api.getExperienceTracker().start(Skill.FISHING);
        api.getExperienceTracker().start(Skill.AGILITY);
    }


}




