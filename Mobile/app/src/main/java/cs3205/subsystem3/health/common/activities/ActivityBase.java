package cs3205.subsystem3.health.common.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.LogWrapper;

import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.FILENAME;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS_STOPPED;

/**
 * Created by Yee on 09/29/17.
 */

public class ActivityBase extends AppCompatActivity {

    public static final String TAG = "ActivityBase";
    private LogWrapper logWrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeLogging();
    }

    /**
     * Set up targets to receive log data
     */
    public void initializeLogging() {
        SharedPreferences prefs = getApplication().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if(!prefs.contains(STEPS_STOPPED)) {
            prefs.edit().putBoolean(STEPS_STOPPED, true).commit();
            prefs.edit().remove(FILENAME);
        }

        if(logWrapper == null) {
            // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
            // Wraps Android's native log framework
            logWrapper = new LogWrapper();
            Log.setLogNode(logWrapper);

            Log.i(TAG, "Ready");
        }
    }
}