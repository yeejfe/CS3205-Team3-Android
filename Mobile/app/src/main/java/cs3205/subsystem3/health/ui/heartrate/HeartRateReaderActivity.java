package cs3205.subsystem3.health.ui.heartrate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.R;

public class HeartRateReaderActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private float heartRate;
    private long timeStamp;
    private ArrayList<Float> heartRates;
    private TextView mHeartRateReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_reader);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
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
        } else {
            Toast.makeText(this, "Sensor Has Already Started.", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop(View view) {
        if (mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this);
            mHeartRateSensor = null;
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
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.upload:
                if (heartRates.size() == 0) {
                    Toast.makeText(this, "Nothing To Upload!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                timeStamp = System.currentTimeMillis();
                HeartRateUploader uploader = new HeartRateUploader();
                uploader.execute(String.valueOf(timeStamp), String.valueOf(computeAverageHeartRate()), this);
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

    class HeartRateUploader extends AsyncTask<Object, Void, Boolean> {
        private Context context;
        final static String UPLOAD_URL = "https://cs3205-3.comp.nus.edu.sg/session/heart?timestamp=" ;
        @Override
        protected Boolean doInBackground(Object... params) {
            String timeStamp = (String)params[0];
            String avgHeartRate = (String)params[1];
            context = (Context)params[2];
            if (upload(timeStamp, avgHeartRate)) {
                return true;
            }
            return false;
        }

        private boolean upload(String timeStamp, String avgHeartRate) {
            String finalUrl = UPLOAD_URL + timeStamp;
            SharedPreferences pref = getSharedPreferences("Token_SharedPreferences", Activity.MODE_PRIVATE);
            String token = pref.getString("access_token", "");
            System.out.println("token in heartrate reader: " + token);
            Invocation.Builder request = ClientBuilder.newClient().target(finalUrl).request();
            Response response = request.header("Authorization", "Bearer " + token).header("x-nfc-token", "hash").post(
                    Entity.entity(avgHeartRate, MediaType.APPLICATION_OCTET_STREAM));
            cs3205.subsystem3.health.common.logger.Log.d("error", response.toString());
            if (response.getStatus() != 200) {
                cs3205.subsystem3.health.common.logger.Log.d("error", response.readEntity(String.class));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            cs3205.subsystem3.health.common.logger.Log.d("result", aBoolean.toString());
            if (aBoolean) {
                heartRates.clear();
                Toast.makeText(context, "Upload Successful.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Upload Failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

}

