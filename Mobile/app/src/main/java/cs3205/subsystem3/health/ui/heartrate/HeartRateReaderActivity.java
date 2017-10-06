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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.utilities.HeartRateUploadTask;

public class HeartRateReaderActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private float heartRate;
    private long timeStamp;
    private ArrayList<Float> heartRates;
    private TextView mHeartRateReading;
    final static String QUERY_PARAMETER_TIMESTAMP = "timestamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_reader);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        heartRates = new ArrayList<>();
        mHeartRateReading = (TextView) findViewById(R.id.hear_rate_reading);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT || sensorEvent.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE) {
            heartRate = sensorEvent.values[0];
            if (heartRate != 0.0) {
                heartRates.add(heartRate);
            }
            mHeartRateReading.setText(String.valueOf(heartRate));
            Log.d("sensorData", "" + sensorEvent.values[0]);
        }
    }

    public void start(View view) {
        if (mHeartRateSensor == null) {
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Sensor Started.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sensor Has Already Started.", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop(View view) {
        if (mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this);
            mHeartRateSensor = null;
            Toast.makeText(this, "Sensor Stopped.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sensor Has Not Started.", Toast.LENGTH_SHORT).show();
        }

    }

    public void clear(View view) {
        heartRates.clear();
        Toast.makeText(this, "Previous Readings Cleared.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mHeartRateSensor,SensorManager.SENSOR_DELAY_NORMAL);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                if (heartRates.size() == 0) {
                    Toast.makeText(this, "Nothing To Upload!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                timeStamp = System.currentTimeMillis();
                new HeartRateUploadTask().execute(String.valueOf(timeStamp), String.valueOf(computeAverageHeartRate()), this);
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

