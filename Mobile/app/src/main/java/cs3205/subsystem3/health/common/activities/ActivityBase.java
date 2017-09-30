package cs3205.subsystem3.health.common.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.LogWrapper;

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
        if(logWrapper == null) {
            // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
            // Wraps Android's native log framework
            logWrapper = new LogWrapper();
            Log.setLogNode(logWrapper);

            Log.i(TAG, "Ready");
        }
    }
}