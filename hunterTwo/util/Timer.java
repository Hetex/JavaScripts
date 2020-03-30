package util;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by Bjorn on 6/05/2015.
 */
public class Timer {

    private Instant start;

    private static final String formatter = "%02d";

    public Timer() {
        reset();
    }

    public void reset() {
        start = Instant.now();
    }

    public Duration duration() {
        return Duration.between(start, Instant.now());
    }

    @Override
    public String toString() {
        Duration duration = duration();
        return String.format(formatter, duration.toHours()) + ":" + String.format(formatter, duration.toMinutes() % 60) + ":" + String.format(formatter, duration.getSeconds() % 60);
    }

}
