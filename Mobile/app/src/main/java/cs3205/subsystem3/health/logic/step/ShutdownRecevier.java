package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;
import cs3205.subsystem3.health.data.source.local.LocalDataSource;
import cs3205.subsystem3.health.data.source.local.StepsDB;

/**
 * Created by Yee on 09/28/17.
 */

public class ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.i(Tag.STEP_SENSOR, "shutting down");

        context.startService(new Intent(context, StepSensorService.class));

        // if the user used a root script for shutdown, the DEVICE_SHUTDOWN
        // broadcast might not be send. Therefore, the app will check this
        // setting on the next boot and displays an error message if it's not
        // set to true
        context.getSharedPreferences("steps", Context.MODE_PRIVATE).edit()
                .putBoolean("correctShutdown", true).commit();

        StepsDB db = (StepsDB) LocalDataSource.getInstance(context);
        // if it's already a new day, add the temp. steps to the last one
        if (db.getSteps(Timestamp.getToday()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            int pauseDifference = steps -
                    context.getSharedPreferences("steps", Context.MODE_PRIVATE)
                            .getInt("pauseCount", steps);
            db.insertNewDay(Timestamp.getToday(), steps - pauseDifference);
            if (pauseDifference > 0) {
                // update pauseCount for the new day
                context.getSharedPreferences("steps", Context.MODE_PRIVATE).edit()
                        .putInt("pauseCount", steps).commit();
            }
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        // current steps will be reset on boot @see BootReceiver
        db.close();
    }

}