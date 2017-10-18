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

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.ui.heartrate.HeartRateReaderActivity;

/**
 * Created by danwen on 7/10/17.
 */

public class HeartRateUploadTask extends AsyncTask<Object, Void, Boolean> {

    private Context context;

    @Override
    protected Boolean doInBackground(Object... params) {
        String tag_username = (String) params[0];
        String nfcTokenHash = (String) params[1];
        String timeStamp = (String)params[2];
        String avgHeartRate = (String)params[3];
        context = (Context)params[4];
        if (upload(tag_username, nfcTokenHash, timeStamp, avgHeartRate)) {
            return true;
        }
        return false;
    }

    private boolean upload(String tag_username, String nfcTokenHash, String timeStamp, String avgHeartRate) {
        SharedPreferences pref = context.getSharedPreferences(Value.KEY_VALUE_SHARED_PREFERENCE, Activity.MODE_PRIVATE);
        String username = pref.getString(Value.KEY_VALUE_SHARED_PREFERENCE_USERNAME, "");
        if (!username.equals(tag_username)) {
            Log.d("HeartRateUploadTask", "username mismatch: " + username + " and " + tag_username);
            return false;
        }
        String token = pref.getString(Value.KEY_VALUE_SHARED_PREFERENCE_ACCESS_TOKEN, "");
        Invocation.Builder request = ClientBuilder.newClient()
                .target(RequestInfo.URL_HEART_RATE_UPLOAD).
                        queryParam(RequestInfo.QUERY_PARAMETER_TIMESTAMP, timeStamp).request();
        Response response = request.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + token).header(
                RequestInfo.HEADER_NFC_TOKEN_HASH, nfcTokenHash).post(
                Entity.entity(avgHeartRate, MediaType.APPLICATION_OCTET_STREAM));
        if (response.getStatus() != 200) {
            Log.d("HeartRateUploadTask", response.toString());
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean isUploadSuccess) {
        if (isUploadSuccess) {
            ((HeartRateReaderActivity) context).clear();
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_SUCCESS, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_FAILURE, Toast.LENGTH_LONG).show();
        }
    }
}
