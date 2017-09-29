package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;
import cs3205.subsystem3.health.data.source.local.Database;
import cs3205.subsystem3.health.data.source.local.LocalDataSource;
import cs3205.subsystem3.health.data.source.local.StepsDB;

/**
 * Created by Yee on 09/28/17.
 */

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = Tag.STEP_SENSOR;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.i(TAG, "booted");

        SharedPreferences prefs = context.getSharedPreferences("steps", Context.MODE_PRIVATE);

        StepsDB db = (StepsDB) LocalDataSource.getInstance(context);

        if (!prefs.getBoolean("correctShutdown", false)) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Incorrect shutdown");
            // can we at least recover some steps?
            int steps = db.getCurrentSteps();
            if (BuildConfig.DEBUG) Log.i(TAG, "Trying to recover " + steps + " steps");
            db.addToLastEntry(steps);
        }
        // last entry might still have a negative step value, so remove that
        // row if that's the case
        db.removeNegativeEntries();
        db.saveCurrentSteps(0);
        db.close();
        prefs.edit().remove("correctShutdown").apply();

        context.startService(new Intent(context, StepSensorService.class));
    }
}
