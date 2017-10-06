package cs3205.subsystem3.health.ui.step;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;
import cs3205.subsystem3.health.data.source.local.StepsDB;
import cs3205.subsystem3.health.logic.step.StepSensorService;

/**
 * Created by Yee on 09/27/17.
 */

public class StepSensorFragment extends Fragment implements SensorEventListener, OnClickListener {

    private TextView stepsView, totalView, averageView, textView;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonUpload;
    private static final String START_SERVICE = "Steps Counter Service Started";
    public static final String STOP_SERVICE = "Press START to count steps";
    private int todayOffset, total_start, since_boot, total_days;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.steps_fragment, null);

        textView = (TextView) view.findViewById(R.id.tv_steps);
        buttonStart = (Button) view.findViewById(R.id.btn_start);
        buttonStop = (Button) view.findViewById(R.id.btn_stop);
        buttonUpload = (Button) view.findViewById(R.id.btn_go_step_upload);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        stepsView = (TextView) view.findViewById(R.id.steps);
        totalView = (TextView) view.findViewById(R.id.total);
        averageView = (TextView) view.findViewById(R.id.average);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startStepsService();
                break;
            case R.id.btn_stop:
                stopStepsService();
                break;
            case R.id.btn_go_step_upload:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_placeholder, new StepUploadFragment(), null);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
    }

    public void startStepsService() {
        getActivity().startService(new Intent(getActivity(), StepSensorService.class));
        textView.setText(START_SERVICE);
        buttonStart.setClickable(false);
        buttonStart.setEnabled(false);
        buttonStop.setClickable(true);
        buttonStop.setEnabled(true);
    }

    public void stopStepsService() {
        getActivity().stopService(new Intent(getActivity(), StepSensorService.class));
        textView.setText(STOP_SERVICE);
        buttonStart.setClickable(true);
        buttonStart.setEnabled(true);
        buttonStop.setClickable(false);
        buttonStop.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        StepsDB db = new StepsDB(getActivity());

        // read todays offset
        todayOffset = db.getSteps(Timestamp.getToday());

        SharedPreferences prefs = getActivity().getSharedPreferences("steps", Context.MODE_PRIVATE);

        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
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

        setTextView(Math.max(todayOffset + since_boot, 0));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (BuildConfig.DEBUG)
            Log.i(Tag.STEP_SENSOR, "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    sensorEvent.sensor.getName());
        if (sensorEvent.values[0] > Integer.MAX_VALUE || sensorEvent.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) sensorEvent.values[0];
            StepsDB db = new StepsDB(getActivity());
            db.insertNewDay(Timestamp.getToday(), (int) sensorEvent.values[0]);
            db.close();
        }
        since_boot = (int) sensorEvent.values[0];

        int steps_today = Math.max(todayOffset + since_boot, 0);

        setTextView(steps_today);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        if (BuildConfig.DEBUG) Log.i(Tag.STEP_SENSOR, sensor.getName() + " accuracy changed: " + i);
    }

    private void setTextView(int steps_today) {
        stepsView.setText(String.valueOf(steps_today));
        totalView.setText(String.valueOf((total_start + steps_today)));
        averageView.setText(String.valueOf(((total_start + steps_today) / total_days)));
    }
}

