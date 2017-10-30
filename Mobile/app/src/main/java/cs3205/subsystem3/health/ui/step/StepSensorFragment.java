package cs3205.subsystem3.health.ui.step;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.data.source.local.Repository;
import cs3205.subsystem3.health.data.source.local.StepsDB;
import cs3205.subsystem3.health.logic.step.StepSensorService;
import cs3205.subsystem3.health.model.Steps;

import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.DIVISOR;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.EVENT_TIMESTAMP;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.FILENAME;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.OFFSET;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.PAUSE_COUNT;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.STEPS_STOPPED;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.SYSTEM_TIMESTAMP;
import static cs3205.subsystem3.health.logic.step.StepsUtil.updateSteps;

/**
 * Created by Yee on 09/27/17.
 */

public class StepSensorFragment extends Fragment implements SensorEventListener, OnClickListener {

    private static final String SESSION_NAME = "session_";

    private String TAG = this.getClass().getName();
    private String sessionName = SESSION_NAME;

    private TextView stepsView, totalView, averageView, textView;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonUpload;
    private static final String START_SERVICE = "Steps Counter Service Started";
    public static final String STOP_SERVICE = "Press START to count steps";
    private int todayOffset, total_start, since_boot, total_days;

    private Steps data;
    private long divisor = 0;
    private long offset = 0;

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

        Log.d(TAG, "onCreateView");

        showSteps();

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
                if (buttonStop.isEnabled()) {
                    Toast.makeText(getActivity(), AppMessage.TOAST_MESSAGE_STOP_BEFORE_UPLOAD, Toast.LENGTH_SHORT).show();
                    return;
                }
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_placeholder, new StepUploadFragment(), null);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
    }

    public void startStepsService() {
        getSessionNameToStart();
    }

    public void stopStepsService() {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        saveSteps();

        prefs.edit().putBoolean(STEPS_STOPPED, true).commit();
        prefs.edit().remove(FILENAME);

        //getActivity().stopService(new Intent(getActivity(), StepSensorService.class));
        setViewServiceStopped();
    }

    @Override
    public void onResume() {
        super.onResume();

        setView();

        StepsDB db = new StepsDB(getActivity());

        // read todays offset
        todayOffset = db.getSteps(Timestamp.getToday());

        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);

        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt(PAUSE_COUNT, since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains(PAUSE_COUNT)) {
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

        int steps_today = Math.max(todayOffset + since_boot, 0);

        Log.d(this.getClass().getName(), "Steps_today: " + steps_today);

        setTextView(steps_today);

        retrieveSteps("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveSteps();
    }

    private void saveSteps() {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            StepsDB db = new StepsDB(getActivity());
            db.saveCurrentSteps(since_boot);
            db.close();

            //save to file
            String filename = prefs.getString(FILENAME, String.valueOf(Timestamp.getEpochTimeMillis()));
            Repository.writeFile(getActivity().getFilesDir().getAbsolutePath(), filename, data);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        getEventOffset(prefs, sensorEvent.timestamp, Timestamp.getEpochTimeMillis());

        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                        +since_boot + " | sensorName: " + sensorEvent.sensor.getName());
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

            long eventTimestamp = getTime(sensorEvent.timestamp);

            data = updateSteps(data, eventTimestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            if (BuildConfig.DEBUG)
                Log.i(TAG, sensor.getName() + " accuracy changed: " + i);
        }
    }

    private void showSteps() {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        setView();

        StepsDB db = new StepsDB(getActivity());

        // read todays offset
        todayOffset = db.getSteps(Timestamp.getToday());

        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt(PAUSE_COUNT, since_boot);

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        int steps_today = Math.max(todayOffset + since_boot, 0);

        Log.d(this.getClass().getName(), "UI - showSteps | Steps_today: " + steps_today + " | " + prefs.getString(FILENAME, "NONE"));

        setTextView(steps_today);
    }

    private void retrieveSteps(String methodName) {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            String filename = prefs.getString(FILENAME, String.valueOf(Timestamp.getEpochTimeMillis()));
            prefs.edit().putString(FILENAME, filename);
            data = Repository.getFile(getActivity().getFilesDir().getAbsolutePath(), filename, sessionName);
        } else {
            data = new Steps(0, sessionName);
            Log.d("Prefs", "new STEPS | method: " + methodName);
        }
    }

    private void setView() {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            setViewServiceStarted();
        } else {
            setViewServiceStopped();
        }
    }

    private void setViewServiceStarted() {
        textView.setText(START_SERVICE);
        buttonStart.setClickable(false);
        buttonStart.setEnabled(false);
        buttonStop.setClickable(true);
        buttonStop.setEnabled(true);
    }

    private void setViewServiceStopped() {
        textView.setText(STOP_SERVICE);
        buttonStart.setClickable(true);
        buttonStart.setEnabled(true);
        buttonStop.setClickable(false);
        buttonStop.setEnabled(false);
    }

    private void setTextView(int steps_today) {
        stepsView.setText(String.valueOf(steps_today));
        totalView.setText(String.valueOf((total_start + steps_today)));
        averageView.setText(String.valueOf(((total_start + steps_today) / total_days)));
    }

    private void getSessionNameToStart() {
        sessionName = SESSION_NAME + Timestamp.getFormattedCurrentTimestamp();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Session Name");

        final EditText input = new EditText(getActivity());
        input.setText(sessionName);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sessionName = input.getText().toString();
                data.setTitle(sessionName);

                SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(STEPS_STOPPED, false).commit();
                prefs.edit().putString(FILENAME, String.valueOf(Timestamp.getEpochTimeMillis())).commit();

                retrieveSteps("startStepsService");

                getActivity().startService(new Intent(getActivity(), StepSensorService.class));
                setViewServiceStarted();
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private long getTime(long eventTimestamp) {
        long eventTimeMillis;
        if (divisor == 0) {
            eventTimeMillis = Timestamp.getEpochTimeMillis();
        } else {
            eventTimeMillis = (eventTimestamp / divisor) + offset;
        }
        return eventTimeMillis;
    }

    private void getEventOffset(SharedPreferences prefs, long nanoEventTimestamp, long sysTimeMillis) {
        if (!prefs.contains(OFFSET) && !prefs.contains(DIVISOR)) {
            if (!prefs.contains(EVENT_TIMESTAMP)) {
                prefs.edit().putLong(EVENT_TIMESTAMP, nanoEventTimestamp);
                prefs.edit().putLong(SYSTEM_TIMESTAMP, sysTimeMillis);
                return;
            }

            long event1TimeStamp = prefs.getLong(EVENT_TIMESTAMP, Timestamp.getEpochTimeMillis());
            long sysTimeMillis1TimeStamp = prefs.getLong(SYSTEM_TIMESTAMP, Timestamp.getEpochTimeMillis());

            long timestampDelta = nanoEventTimestamp - event1TimeStamp;
            long sysTimeDelta = sysTimeMillis - sysTimeMillis1TimeStamp;

            long divisor;
            long offset;
            if (timestampDelta / sysTimeDelta > 1000) { // in reality ~1 vs ~1,000,000
                // timestamps are in nanoseconds
                divisor = 1000000;
            } else {
                // timestamps are in milliseconds
                divisor = 1;
            }

            offset = sysTimeMillis1TimeStamp - (event1TimeStamp / divisor);

            prefs.edit().putLong(OFFSET, offset);
            prefs.edit().putLong(DIVISOR, divisor);
        } else {
            offset = prefs.getLong(OFFSET, 1000);
            divisor = prefs.getLong(DIVISOR, 1);
        }
    }
}
