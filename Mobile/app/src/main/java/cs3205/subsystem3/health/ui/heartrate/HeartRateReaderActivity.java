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
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_reader);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        heartRates = new ArrayList<>();
        context = this;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT || sensorEvent.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE) {
            heartRate = sensorEvent.values[0];
            heartRates.add(heartRate);
            Log.d("sensorData", "" + sensorEvent.values[0]);
        }
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
                timeStamp = System.currentTimeMillis();
                HeartRateUploader uploader = new HeartRateUploader();
                uploader.execute(String.valueOf(timeStamp), String.valueOf(computeAverageHeartRate()));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private float computeAverageHeartRate() {
        float sum = 0;
        for (float heartRate : heartRates) {
            sum += heartRate;
        }
        return Math.round(sum / heartRates.size());
    }

    class HeartRateUploader extends AsyncTask<String, Void, Boolean> {
        final static String UPLOAD_URL = "https://cs3205-3.comp.nus.edu.sg/upload/heart/" ;
        @Override
        protected Boolean doInBackground(String... params) {
            String timeStamp = params[0];
            String heartRate = params[1];
            if (upload(timeStamp, heartRate)) {
                return true;
            }
            return false;
        }

        private boolean upload(String timeStamp, String heartRate) {
            String finalUrl = UPLOAD_URL + timeStamp;
      //      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences pref = getSharedPreferences("Token_SharedPreferences", Activity.MODE_PRIVATE);
            String token = pref.getString("access_token", "");
            Invocation.Builder request = ClientBuilder.newClient().target(finalUrl).request();
            Response response = request.header("Authorization", "Bearer " + token).header("x-nfc-token", "hash").post(
                    Entity.entity(heartRate, MediaType.APPLICATION_OCTET_STREAM));
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
        }
    }

}

