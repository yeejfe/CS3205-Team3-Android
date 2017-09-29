package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Yee on 09/28/17.
 */

public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        SharedPreferences prefs =
                context.getSharedPreferences("steps", Context.MODE_MULTI_PROCESS);
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) &&
                !prefs.contains("pauseCount")) {
            // if power connected & not already paused, then pause now
            context.startService(new Intent(context, StepSensorService.class)
                    .putExtra("action", StepSensorService.ACTION_PAUSE));
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction()) &&
                prefs.contains("pauseCount")) {
            // if power disconnected & currently paused, then resume now
            context.startService(new Intent(context, StepSensorService.class)
                    .putExtra("action", StepSensorService.ACTION_PAUSE));
        }
    }
}