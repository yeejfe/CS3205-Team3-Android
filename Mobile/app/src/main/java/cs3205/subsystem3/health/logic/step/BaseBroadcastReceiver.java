package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Yee on 09/30/17.
 */

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    protected static String PREF_STEPS = "steps";
    protected static String CORRECT_SHUTDOWN = "correctShutdown";
    protected static String PAUSE_COUNT = "pauseCount";
    protected static String ACTION = "action";

    boolean checkServiceStoppedPref(Context context){
        boolean isServiceStopped = true;
        SharedPreferences prefs = context.getSharedPreferences(PREF_STEPS, Context.MODE_PRIVATE);
        isServiceStopped = prefs.getBoolean("StepsStopped", true);

        return isServiceStopped;
    }
}
