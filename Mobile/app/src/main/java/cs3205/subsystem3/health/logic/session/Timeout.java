package cs3205.subsystem3.health.logic.session;

/**
 * Created by Yee on 11/01/17.
 */

public class Timeout {
    public static final int DEFAULT_COUNT = 0;
    public static final int DEFAULT_TIMEOUT_IN_SECONDS = 5; //in seconds
    public static final int MULTIPLIER_TO_MILLISECONDS = 1000;

    private int count;
    private int duration;

    private static Timeout timeout;

    private Timeout() {
        count = DEFAULT_COUNT;
        duration = DEFAULT_TIMEOUT_IN_SECONDS * MULTIPLIER_TO_MILLISECONDS;
    }

    public static Timeout getInstance() {
        if (timeout == null) {
            timeout = new Timeout();
        }
        return timeout;
    }

    public int getCount() {
        return ++count;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration * MULTIPLIER_TO_MILLISECONDS;
    }

    public void reset() {
        timeout = new Timeout();
    }
}
