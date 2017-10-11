package cs3205.subsystem3.health.common.utilities;

import android.content.Context;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by danwen on 11/10/17.
 */

public class SessionManager {
    private static Timer timer;
    private static final int TIMER_DELAY_IN_MINUTES = 1;

    public static boolean isTimerSet() {
        return timer != null;
    }

    public static void setTimer(Context context) {
        timer = new Timer();
        timer.schedule(new LogoutTimerTask(context), TimeUnit.MINUTES.toMillis(TIMER_DELAY_IN_MINUTES));
    }

    public static void resetTimer(Context context) {
        cancelTimer();
        setTimer(context);
    }

    public static void cancelTimer() {
       timer.cancel();
       timer = null;
    }

}
