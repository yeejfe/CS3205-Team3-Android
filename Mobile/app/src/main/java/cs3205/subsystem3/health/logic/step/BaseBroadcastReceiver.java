package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;

import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS_STOPPED;

/**
 * Created by Yee on 09/30/17.
 */

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    protected static String CORRECT_SHUTDOWN = "correctShutdown";

    boolean checkServiceStoppedPref(Context context){
        boolean isServiceStopped = true;
        SharedPreferences prefs = context.getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        isServiceStopped = prefs.getBoolean(STEPS_STOPPED, true);

        return isServiceStopped;
    }
}
