package util;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

public class DefaultAPI extends MethodProvider{

    public Settings settings;
    private ReactionGenerator reactionGenerator;

    public DefaultAPI(Bot bot, Settings sm, ReactionGenerator rg)
    {
        super.exchangeContext(bot);
        this.settings = sm;
        this.reactionGenerator = rg;
    }

    public double getReactionPredict()
    {
        double reaction = reactionGenerator.nextReactionTime(30,10,0.02,0.01,30,500);
        return reaction;
    }

    public double getReactionNormal()
    {
        double reaction = reactionGenerator.nextReactionTime(200,50,0.007,0.2,100,3000);
        return reaction;
    }

    public double getReactionAfk()
    {
        double reaction =  reactionGenerator.nextReactionTime(5000,2000,5,0.1,200,15000);
        settings.setTimer(new Timer());
        settings.setTimeRemaing((int)reaction);
        return reaction;
    }

    public final String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

}
