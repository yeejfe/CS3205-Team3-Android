package cs3205.subsystem3.health.logic.step;

import android.content.Context;
import android.content.Intent;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;

/**
 * Created by Yee on 09/28/17.
 */

public class AppUpdatedReceiver extends BaseBroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG)
            Log.i(Tag.STEP_SENSOR, "app updated");
        context.startService(new Intent(context, StepSensorService.class));
    }
}