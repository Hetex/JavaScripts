package util;

public class Settings {

    private Timer timer;
    private int timeRemaining = 0;

    public int getTimeRemaining ()
    {
        return (timeRemaining - (int)(timer.duration().toMillis()));
    }

    public void setTimeRemaing(int time)
    {
        this.timeRemaining = time;
    }

    public void setTimer(Timer timer)
    {
        this.timer = timer;
    }

}
