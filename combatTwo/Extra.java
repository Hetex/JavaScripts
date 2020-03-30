import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import util.ReactionGenerator;

import static org.rspeer.runetek.api.commons.math.Random.nextInt;

public class Extra {

    ReactionGenerator reactionGenerator;
    int combatCount = 0;

    public Extra()
    {
        reactionGenerator = new ReactionGenerator();
    }

    public double getReactionPredict() {
        double reaction = reactionGenerator.nextReactionTime(30, 10, 0.02, 0.01, 30, 500);
        return reaction;
    }

    public double getReactionNormal() {
        double reaction = reactionGenerator.nextReactionTime(300, 50, 0.007, 0.2, 100, 3000);
        Log.info("NORMAL: " + reaction);
        return reaction;
    }

    public double getReactionAfk() {
        double reaction = reactionGenerator.nextReactionTime(5000, 2000, 5, 0.1, 300, 15000);
        Log.info("AFK: " + reaction);
        return reaction;
    }

    public double getAttackAfk()
    {
        double reaction = reactionGenerator.nextReactionTime(2000,1000,2.5,0.05,300,7000);
        Log.info("AFK ATTACK: " + reaction);
        return reaction;
    }


    private int[] dropOrder() {
        int[] drop1 = new int[]{0, 4, 8, 12, 16, 20, 24, 25, 21, 17, 13, 9, 5, 1, 2, 6, 10, 14, 18, 22, 26, 27, 23, 19, 15, 11, 7, 3};//pipe her up down
        int[] drop2 = new int[]{0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20, 21, 24, 25, 26, 27, 22, 23, 18, 19, 14, 15, 10, 11, 6, 7, 2, 3};//pencil ziggy
        int[] drop3 = new int[]{0, 1, 2, 3, 7, 6, 5, 4, 8, 9, 10, 11, 15, 14, 13, 12, 16, 17, 18, 19, 23, 22, 21, 20, 24, 25, 26, 27};//get long ziggy

        switch (nextInt(1, 4)) {
            case 1:
                Log.info("Drop 1");
                return drop1;
            case 2:
                Log.info("Drop 2");
                return drop2;
            case 3:
                Log.info("Drop 3");
                return drop3;
        }

        return null;

    }

    public final String formatTime(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public boolean inCombat()
    {
        Player local = Players.getLocal();

        Npc npc = Npcs.getNearest(x -> x.getTarget() == local && x.containsAction("Attack"));

        if(!local.isHealthBarVisible())
        {
            combatCount++;

            if(combatCount > 300)
            {
                Log.info("Combat count");
                return false;
            }
        }
        else
        {
            combatCount = 0;
        }

        return local.getTargetIndex() != -1 || npc != null || local.getTarget() != null;
    }
}
