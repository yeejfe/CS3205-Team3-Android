package cs3205.subsystem3.health.ui.heartrate;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.utilities.HeartRateUploadTask;
import cs3205.subsystem3.health.common.utilities.LogoutHelper;
import cs3205.subsystem3.health.common.utilities.SessionManager;

public class HeartRateReaderActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private float heartRate;
    private ArrayList<Float> heartRates;
    private TextView mHeartRateReading;
    private Button mStart;
    private Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_reader);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        heartRates = new ArrayList<>();
        mHeartRateReading = (TextView) findViewById(R.id.hear_rate_reading);
        mStart = (Button) findViewById(R.id.start_sensor);
        mStop = (Button) findViewById(R.id.stop_sensor);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT || sensorEvent.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE) {
            heartRate = sensorEvent.values[0];
            if (heartRate != 0.0) {
                heartRates.add(heartRate);
            }
            mHeartRateReading.setText(String.valueOf(heartRate));
        }
    }

    public void start(View view) {

        if (mHeartRateSensor == null) {
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_ACTIVATE_SENSOR, Toast.LENGTH_SHORT).show();
        }
        mStart.setEnabled(false);
        mStop.setEnabled(true);
    }

    public void stop(View view) {
        if (mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this);
            mHeartRateSensor = null;
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_DEACTIVATE_SENSOR, Toast.LENGTH_SHORT).show();
        }
        mStop.setEnabled(false);
        mStart.setEnabled(true);
    }

    public void clear(View view) {
        heartRates.clear();
        Toast.makeText(this, AppMessage.TOAST_MESSAGE_CLEAR_PREVIOUS_CHANGES, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.isSessionValid(this)) {
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_SESSION_EXPIRED, Toast.LENGTH_LONG).show();
            LogoutHelper.logout(this);
        } else {
            mSensorManager.registerListener(this, mHeartRateSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload, menu);
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                if (heartRates.size() == 0) {
                    Toast.makeText(this, AppMessage.TOAST_MESSAGE_NOTHING_TO_UPLOAD, Toast.LENGTH_SHORT).show();
                    return false;
                }
                new HeartRateUploadTask().execute(String.valueOf(System.currentTimeMillis()), String.valueOf(computeAverageHeartRate()), this);
                return true;
            case R.id.logout:
                LogoutHelper.logout(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private int computeAverageHeartRate() {
        float sum = 0;
        for (float heartRate : heartRates) {
            sum += heartRate;
        }
        return Math.round(sum / heartRates.size());
    }

    public void clear() {
        heartRates.clear();
    }

}

