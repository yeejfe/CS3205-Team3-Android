package cs3205.subsystem3.health.logic.step;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.ACTION;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.ACTION_PAUSE;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.PAUSE_COUNT;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS;

/**
 * Created by Yee on 09/28/17.
 */

public class PowerReceiver extends BaseBroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        SharedPreferences prefs =
                context.getSharedPreferences(STEPS, Context.MODE_MULTI_PROCESS);
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) &&
                !prefs.contains(PAUSE_COUNT)) {
            // if power connected & not already paused, then pause now
            context.startService(new Intent(context, StepSensorService.class)
                    .putExtra(ACTION, ACTION_PAUSE));
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction()) &&
                prefs.contains(PAUSE_COUNT)) {
            // if power disconnected & currently paused, then resume now
            context.startService(new Intent(context, StepSensorService.class)
                    .putExtra(ACTION, ACTION_PAUSE));
        }
    }
}