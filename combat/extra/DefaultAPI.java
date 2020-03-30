package extra;

import combat.Settings;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

public class DefaultAPI extends MethodProvider{

    private Settings settings;
    private Extra extra;
    private ReactionGenerator reactionGenerator;

    private NPC nextMob;

    public DefaultAPI(Bot bot, Settings sm, Extra ex, ReactionGenerator rg)
    {
        super.exchangeContext(bot);
        this.settings = sm;
        this.extra = ex;
        this.reactionGenerator = rg;
    }

    public Settings getExtraSettings() {
        return settings;
    }

    public Extra getExtra() {
        return extra;
    }

    public double getReactionPredict()
    {
        return reactionGenerator.nextReactionTime(30,10,0.02,0.01,30,500);
    }

    public double getReactionNormal()
    {
        return reactionGenerator.nextReactionTime(200,50,0.007,0.2,100,5000);
    }

    public double getReactionAfk()
    {
        return reactionGenerator.nextReactionTime(5000,2000,5,0.1,200,10000);
    }

    public NPC getNextMob(boolean recalc)
    {
        if(nextMob == null || recalc || this.myPlayer().isInteracting(nextMob))
        {
            nextMob = this.getNpcs().closest(npc ->
                    Arrays.asList(this.getExtraSettings().getNpcs()).contains(npc.getName())
                            && npc.hasAction("Attack")
                            && !npc.isUnderAttack()
                            && (npc.getInteracting() == null || npc.getInteracting() == this.myPlayer())
                            && npc.getHealthPercent() > 50
                            && this.getExtraSettings().getFightArea().contains(npc.getX(), npc.getY()));
        }

        return nextMob;
    }
}
