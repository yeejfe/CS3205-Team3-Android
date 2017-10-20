package cs3205.subsystem3.health.ui.heartrate;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.Crypto;
import cs3205.subsystem3.health.common.utilities.HeartRateUploadTask;
import cs3205.subsystem3.health.common.utilities.LogoutHelper;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;

public class HeartRateReaderActivity extends AppCompatActivity implements SensorEventListener{

    private final Handler mHandler = new Handler();
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private float heartRate;
    private ArrayList<Float> heartRates;
    private TextView mHeartRateReading;
    private Button mStart;
    private Button mStop;
    private GraphView mGraph;
    private LineGraphSeries<DataPoint> mSeries;
    private Runnable mGraphUpdater;
    private int mCounter;
    private static final DecimalFormat df = new DecimalFormat("#.#");
    private static final DataPoint[] emptyDataPoints = new DataPoint[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_reader);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        heartRates = new ArrayList<>();
        mHeartRateReading = (TextView) findViewById(R.id.hear_rate_reading);
        mStart = (Button) findViewById(R.id.start_sensor);
        mStop = (Button) findViewById(R.id.stop_sensor);

        mGraph = (GraphView) findViewById(R.id.graph);
        GridLabelRenderer glr= mGraph.getGridLabelRenderer();
        glr.setPadding(80);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(8);
        mSeries = new LineGraphSeries<>();
        mGraph.addSeries(mSeries);
        for (int i = 0; i < emptyDataPoints.length; i++) {
            emptyDataPoints[i] = new DataPoint(0, 0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT || sensorEvent.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE) {
            heartRate = sensorEvent.values[0];
            if (heartRate != 0.0) {
                heartRates.add(heartRate);
            }
            mHeartRateReading.setText(String.valueOf(df.format(heartRate)));
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
        mHandler.postDelayed(mGraphUpdater, 300);
    }

    public void stop(View view) {
        if (mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this);
            mHeartRateSensor = null;
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_DEACTIVATE_SENSOR, Toast.LENGTH_SHORT).show();
        }
        mStop.setEnabled(false);
        mStart.setEnabled(true);
        mHandler.removeCallbacks(mGraphUpdater);
        mSeries.resetData(emptyDataPoints);
        mHeartRateReading.setText(String.valueOf(0));
    }

    public void clear(View view) {
        if (mStop.isEnabled()) {
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_STOP_BEFORE_CLEAR, Toast.LENGTH_SHORT).show();
        } else {
            heartRates.clear();
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_CLEAR_PREVIOUS_CHANGES, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        switch (i) {
            case -1:
                Log.d("HeartRateReaderActivity", "sensor accuracy: no contact");
            case 0:
                Log.d("HeartRateReaderActivity", "sensor accuracy: unreliable");
            case 1:
                Log.d("HeartRateReaderActivity", "sensor accuracy: low");
            case 2:
                Log.d("HeartRateReaderActivity", "sensor accuracy: medium");
            case 3:
                Log.d("HeartRateReaderActivity", "sensor accuracy: high");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.isTimerSet()) {
            SessionManager.cancelTimer();
        }
        mSensorManager.registerListener(this, mHeartRateSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mGraphUpdater = new Runnable() {
            @Override
            public void run() {
                mSeries.appendData(new DataPoint(++mCounter, Math.round(heartRate)), true, 8);
                mHandler.postDelayed(this, 300);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mGraphUpdater);
        if (SessionManager.isTimerSet()) {
            SessionManager.resetTimer(this);
        } else {
            SessionManager.setTimer(this);
        }
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
                if (mStop.isEnabled()) {
                    Toast.makeText(this, AppMessage.TOAST_MESSAGE_STOP_BEFORE_UPLOAD, Toast.LENGTH_SHORT).show();
                    return false;
                } else if (heartRates.size() == 0) {
                    Toast.makeText(this, AppMessage.TOAST_MESSAGE_NOTHING_TO_UPLOAD, Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent startNFCReadingActivity = new Intent(this, NFCReaderActivity.class);
                startActivityForResult(startNFCReadingActivity, 50);
                return true;
            case R.id.logout:
                LogoutHelper.logout(this, AppMessage.TOAST_MESSAGE_LOGOUT_SUCCESS);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50) {
            String tag_username = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_USERNAME);
            String tag_password = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
            try {
                new HeartRateUploadTask().execute(tag_username, Crypto.generateTOTP(tag_password),
                        String.valueOf(System.currentTimeMillis()), String.valueOf(computeAverageHeartRate()), this);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(this, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                Toast.makeText(this, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

