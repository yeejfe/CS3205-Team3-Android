package cs3205.subsystem3.health.logic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;


/**
 * Created by Yee on 10/05/17.
 */

public class StepCountUploader extends AsyncTask<String, Void, Boolean> {
    public static final String TOKEN_SHARED_PREFERENCES = "Token_SharedPreferences";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EMPTY_STRING = "";
    public static final String HASH = "hash";
    public static final String ERROR = "error";
    public static final String RESULT = "result";
    public static final int STATUS_200 = 200;

    private SharedPreferences preferences;
    private InputStream inputStream;

    public StepCountUploader(SharedPreferences preferences, File file) throws FileNotFoundException {
        super();
        this.preferences = preferences;
        this.inputStream = new FileInputStream(file);
    }

    protected Boolean doInBackground(String... params) {
        String timeStamp = params[0];
        String stepCount = params[1];
        if (upload(preferences)) {
            return true;
        }
        return false;
    }

    private boolean upload(SharedPreferences sPref) {
        SharedPreferences pref = sPref;
        String token = pref.getString(ACCESS_TOKEN, EMPTY_STRING);
        RemoteDataSource rDS = new RemoteDataSource();
        Response response = rDS.buildFileUploadRequest(inputStream, token, HASH, Timestamp.getEpochTimeStamp(), RemoteDataSource.Type.STEPS);
        rDS.close();
        Log.d(ERROR, response.toString());
        if (response.getStatus() != STATUS_200) {
            Log.d(ERROR, response.readEntity(String.class));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.d(RESULT, aBoolean.toString());
    }
}