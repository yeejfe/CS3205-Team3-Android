package cs3205.subsystem3.health.common.utilities;

import android.os.AsyncTask;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by danwen on 5/10/17.
 */

public class HeartRateUploader extends AsyncTask<String, Void, Boolean> {
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
        String finalUrl = UPLOAD_URL + "/" + timeStamp;
        Invocation.Builder request = ClientBuilder.newClient().target(finalUrl).request();
        Response response = request.header("Authorization", "Bearer ").header("x-nfc-token", "hash").post(
                Entity.entity(heartRate, MediaType.APPLICATION_OCTET_STREAM));
        Log.d("error", response.toString());
        if (response.getStatus() != 200) {
            Log.d("error", response.readEntity(String.class));
            return false;
        } else {
           return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.d("result", aBoolean.toString());
    }
}
