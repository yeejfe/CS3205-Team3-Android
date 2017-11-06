package cs3205.subsystem3.health.ui.step;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private static final int PAST_DAYS = 8;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E", Locale.getDefault());
    private static final String MSG_STOP = "Stopping Session";
    private static final String TITLE = "Session Name";

    private String TAG = this.getClass().getName();
    private String sessionName = SESSION_NAME;

    private TextView stepsView, totalView, averageView, textView, avgLabel, todayLabel, totalLabel;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonUpload;
    private BarChart barChart;
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
        barChart = (BarChart) view.findViewById(R.id.barchart);
        avgLabel = (TextView) view.findViewById(R.id.avg_label);
        todayLabel = (TextView) view.findViewById(R.id.today_label);
        totalLabel = (TextView) view.findViewById(R.id.total_label);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        stepsView = (TextView) view.findViewById(R.id.steps);
        totalView = (TextView) view.findViewById(R.id.total);
        averageView = (TextView) view.findViewById(R.id.average);

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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(MSG_STOP);

        ProgressBar progressbar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        progressbar.setIndeterminate(true);
        progressbar.setVisibility(View.VISIBLE);

        alertDialogBuilder.setView(progressbar);
        alertDialogBuilder.setCancelable(false);
        //alertDialogBuilder.setMessage(MSG_STOP);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


//        Thread thread = new Thread(new SaveSteps(alertDialog));
//        thread.start();

        saveSteps();

        prefs.edit().putBoolean(STEPS_STOPPED, true).commit();
        prefs.edit().remove(FILENAME);

        //getActivity().stopService(new Intent(getActivity(), StepSensorService.class));
        setViewServiceStopped();
        alertDialog.dismiss();
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

        //saveSteps();
    }

    private void saveSteps() {
        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(STEPS_STOPPED, false) == false) {
            StepsDB db = new StepsDB(getActivity());
            db.saveCurrentSteps(since_boot);
            db.close();

            //save to file
            String filename = prefs.getString(FILENAME, String.valueOf(Timestamp.getEpochTimeMillis()));
            Repository.writeFile(getActivity(), getActivity().getFilesDir().getAbsolutePath(), filename, data);
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
        if (barChart.getData().size() > 0) barChart.clearChart();

        SharedPreferences prefs = getActivity().getSharedPreferences(STEPS, Context.MODE_PRIVATE);
        setView();

        BarModel barModel;
        StepsDB db = new StepsDB(getActivity());

        // read todays offset
        todayOffset = db.getSteps(Timestamp.getToday());

        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt(PAUSE_COUNT, since_boot);

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        List<Pair<Long, Integer>> days = db.getLastEntries(PAST_DAYS);

        db.close();

        //update bar chart
        int steps;
        long lastestTime;
        ArrayList<BarModel> barModels = new ArrayList<>();
        if(days.size() != 0) {
            lastestTime = days.get(days.size() - 1).first;
        } else {
            lastestTime = Timestamp.getEpochTimeMillis() - Timestamp.EPOCH_DIFF;
        }

        for(int i = 0; i < PAST_DAYS - days.size(); i++){
            lastestTime -= Timestamp.EPOCH_DIFF;
            barModels.add(new BarModel(DATE_FORMAT.format(new Date(lastestTime)), 0, Color.parseColor("#827717")));
        }

        for(int i = barModels.size() - 1; i > 0; i--){
            barChart.addBar(barModels.get(i));
        }

        for (int i = days.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = days.get(i);
            steps = current.second;
            Log.d(TAG, "steps = " + String.valueOf(steps));
            barModel = new BarModel(DATE_FORMAT.format(new Date(current.first)), 0, Color.parseColor("#827717"));
            if (steps > 0) {
                barModel = new BarModel(DATE_FORMAT.format(new Date(current.first)), 0,
                        steps > 1500 ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
                barModel.setValue(steps);
            }
            barChart.addBar(barModel);
        }

        Log.d(TAG, "barChart = " + String.valueOf(barChart.getData().size()));

        if (barChart.getData().size() > 0) {
            barChart.setVisibility(View.VISIBLE);
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }

        int steps_today = Math.max(todayOffset + since_boot, 0);

        Log.d(this.getClass().getName(), "UI - showSteps | Steps_today: " + steps_today + " | " + prefs.getString(FILENAME, "NONE"));

        setTextView(steps_today);
        setParams();
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

    private void setParams() {
        if (barChart.getVisibility() == View.VISIBLE) {
            Resources r = getActivity().getResources();

            RelativeLayout.LayoutParams totalParams = (RelativeLayout.LayoutParams) totalLabel.getLayoutParams();
            totalParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    23,
                    r.getDisplayMetrics()
            );
            ;
            RelativeLayout.LayoutParams avgParams = (RelativeLayout.LayoutParams) avgLabel.getLayoutParams();
            avgParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    75,
                    r.getDisplayMetrics()
            );
            ;
            RelativeLayout.LayoutParams todayParams = (RelativeLayout.LayoutParams) todayLabel.getLayoutParams();
            todayParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    123,
                    r.getDisplayMetrics()
            );
            ;
        } else {
            Resources r = getActivity().getResources();

            RelativeLayout.LayoutParams totalParams = (RelativeLayout.LayoutParams) totalLabel.getLayoutParams();
            totalParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    153,
                    r.getDisplayMetrics()
            );
            ;
            RelativeLayout.LayoutParams avgParams = (RelativeLayout.LayoutParams) avgLabel.getLayoutParams();
            avgParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    205,
                    r.getDisplayMetrics()
            );
            ;
            RelativeLayout.LayoutParams todayParams = (RelativeLayout.LayoutParams) todayLabel.getLayoutParams();
            todayParams.topMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    253,
                    r.getDisplayMetrics()
            );
            ;
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
        alertDialogBuilder.setTitle(TITLE);

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

                //TODO: Change to service instead of Intent Service
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

    private class SaveSteps implements Runnable {
        private AlertDialog alertDialog;

        SaveSteps(AlertDialog alertDialog) {
            this.alertDialog = alertDialog;
        }

        @Override
        public void run() {
            saveSteps();

            alertDialog.dismiss();
        }
    }
}
