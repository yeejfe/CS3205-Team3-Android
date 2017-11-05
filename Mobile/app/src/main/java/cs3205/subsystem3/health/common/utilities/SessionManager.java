package cs3205.subsystem3.health.common.utilities;

import android.content.Context;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by danwen on 11/10/17.
 */

public class SessionManager {
    private static Timer logoutTimer;
    private static final int LOGOUT_TIMER_DELAY_IN_MINUTES = 10;

    public static boolean isLogoutTimerSet() {
        return logoutTimer != null;
    }

    public static void setLogoutTimer(Context context) {
        logoutTimer = new Timer();
        logoutTimer.schedule(new LogoutTimerTask(context), TimeUnit.MINUTES.toMillis(LOGOUT_TIMER_DELAY_IN_MINUTES));
    }

    public static void resetLogoutTimer(Context context) {
        cancelLogoutTimer();
        setLogoutTimer(context);
    }

    public static void cancelLogoutTimer() {
       logoutTimer.cancel();
       logoutTimer = null;
    }

}
