package cs3205.subsystem3.health.logic.step;

import android.content.Context;
import android.content.Intent;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;
import cs3205.subsystem3.health.data.source.local.StepsDB;

import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.PAUSE_COUNT;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS;

/**
 * Created by Yee on 09/28/17.
 */

public class ShutdownReceiver extends BaseBroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.i(Tag.STEP_SENSOR, "shutting down");

        if (checkServiceStoppedPref(context) == false)
            context.startService(new Intent(context, StepSensorService.class));
        else
            return;

        // if the user used a root script for shutdown, the DEVICE_SHUTDOWN
        // broadcast might not be send. Therefore, the app will check this
        // setting on the next boot and displays an error message if it's not
        // set to true
        context.getSharedPreferences(STEPS, Context.MODE_PRIVATE).edit()
                .putBoolean(CORRECT_SHUTDOWN, true).commit();

        StepsDB db = new StepsDB(context);
        // if it's already a new day, add the temp. steps to the last one
        if (db.getSteps(Timestamp.getToday()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            int pauseDifference = steps -
                    context.getSharedPreferences(STEPS, Context.MODE_PRIVATE)
                            .getInt(PAUSE_COUNT, steps);
            db.insertNewDay(Timestamp.getToday(), steps - pauseDifference);
            if (pauseDifference > 0) {
                // update pauseCount for the new day
                context.getSharedPreferences(STEPS, Context.MODE_PRIVATE).edit()
                        .putInt(PAUSE_COUNT, steps).commit();
            }
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        // current steps will be reset on boot @see BootReceiver
        db.close();
    }

}