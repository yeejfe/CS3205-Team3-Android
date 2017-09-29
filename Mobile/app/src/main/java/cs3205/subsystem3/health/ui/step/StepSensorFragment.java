package cs3205.subsystem3.health.ui.step;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;
import cs3205.subsystem3.health.data.source.local.LocalDataSource;
import cs3205.subsystem3.health.data.source.local.StepsDB;
import cs3205.subsystem3.health.logic.step.StepSensorService;

/**
 * Created by Yee on 09/27/17.
 */

public class StepSensorFragment extends FragmentActivity implements SensorEventListener {

    private TextView stepsView, totalView, averageView, textView;
    private Button buttonStart;
    private Button buttonStop;
    private static final String START_SERVICE = "Steps Counter Service Started";
    public static final String STOP_SERVICE = "Press START to count steps";
    private int todayOffset, total_start, since_boot, total_days;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView = (TextView) findViewById(R.id.tv_steps);
        buttonStart = (Button) findViewById(R.id.btn_start);
        buttonStop = (Button) findViewById(R.id.btn_stop);

        stepsView = (TextView) findViewById(R.id.steps);
        totalView = (TextView) findViewById(R.id.total);
        averageView = (TextView) findViewById(R.id.total);
    }

    @Override
    public void onResume() {
        super.onResume();

        StepsDB db = (StepsDB) LocalDataSource.getInstance(getApplicationContext());

        // read todays offset
        todayOffset = db.getSteps(Timestamp.getToday());

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("steps", Context.MODE_PRIVATE);

        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor == null) {
                //error
            } else {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            }
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();
    }

    public void startStepsService(View view) {
        startService(new Intent(this, StepSensorService.class));
        textView.setText(START_SERVICE);
        buttonStart.setClickable(false);
        buttonStop.setClickable(true);
    }

    public void stopStepsService(View view) {
        stopService(new Intent(this, StepSensorService.class));
        textView.setText(STOP_SERVICE);
        buttonStart.setClickable(true);
        buttonStop.setClickable(false);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (BuildConfig.DEBUG)
            Log.i(Tag.STEP_SENSOR, "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    sensorEvent.values[0]);
        if (sensorEvent.values[0] > Integer.MAX_VALUE || sensorEvent.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) sensorEvent.values[0];
            StepsDB db = (StepsDB) LocalDataSource.getInstance(getApplicationContext());
            db.insertNewDay(Timestamp.getToday(), (int) sensorEvent.values[0]);
            db.close();
        }
        since_boot = (int) sensorEvent.values[0];

        int steps_today = Math.max(todayOffset + since_boot, 0);

        stepsView.setText(steps_today);
        totalView.setText((total_start + steps_today));
        averageView.setText(((total_start + steps_today) / total_days));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //will not happen
    }
}

