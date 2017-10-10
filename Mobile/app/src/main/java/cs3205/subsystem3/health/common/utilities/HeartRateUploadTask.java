package cs3205.subsystem3.health.common.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.ui.heartrate.HeartRateReaderActivity;

/**
 * Created by danwen on 7/10/17.
 */

public class HeartRateUploadTask extends AsyncTask<Object, Void, Boolean> {

    final static String UPLOAD_URL = "https://cs3205-3.comp.nus.edu.sg/session/heart" ;
    final static String QUERY_PARAMETER_TIMESTAMP = "timestamp";

    private Context context;

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
        SharedPreferences pref = context.getSharedPreferences(Value.KEY_VALUE_SHARED_PREFERENCE_TOKEN, Activity.MODE_PRIVATE);
        String token = pref.getString(Value.KEY_VALUE_SHARED_PREFERENCE_ACCESS_TOKEN, "");
        String nfcTokenHash = pref.getString(Value.KEY_VALUE_SHARED_PREFERENCE_NFC_HASH, "");
        System.out.println("token in heartrate reader: " + token);
        Invocation.Builder request = ClientBuilder.newClient().target(UPLOAD_URL).queryParam(QUERY_PARAMETER_TIMESTAMP, timeStamp).request();
        Response response = request.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + token).header(
                RequestInfo.HEADER_NFC_TOKEN_HASH, nfcTokenHash).post(
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
    protected void onPostExecute(Boolean isUploadSuccess) {
        cs3205.subsystem3.health.common.logger.Log.d("result", isUploadSuccess.toString());
        if (isUploadSuccess) {
            ((HeartRateReaderActivity) context).clear();
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_SUCCESS, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_FAILURE, Toast.LENGTH_LONG).show();
        }
    }
}
