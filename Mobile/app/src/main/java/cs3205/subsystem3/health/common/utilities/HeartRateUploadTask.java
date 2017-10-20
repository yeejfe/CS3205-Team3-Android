package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
        String tag_password = (String) params[1];
        String timeStamp = (String)params[2];
        String avgHeartRate = (String)params[3];
        context = (Context)params[4];
        if (upload(tag_username, tag_password, timeStamp, avgHeartRate)) {
            return true;
        }
        return false;
    }

    private boolean upload(String tag_username, String tag_password, String timeStamp, String avgHeartRate) {
        SecurePreferences securePreferences = new SecurePreferences(context.getApplicationContext(),
                tag_password, Value.CUSTOM_SHARED_PREFERENCE_FILENAME);
        String username = securePreferences.getString(Value.KEY_VALUE_SHARED_PREFERENCE_USERNAME, "");

        if (!username.equals(tag_username)) {
            Log.d("HeartRateUploadTask", "username mismatch: " + username + " and " + tag_username);
            return false;
        }
        String accessToken = securePreferences.getString(Value.KEY_VALUE_SHARED_PREFERENCE_ACCESS_TOKEN, "");
        Invocation.Builder request = ClientBuilder.newClient()
                .target(RequestInfo.URL_HEART_RATE_UPLOAD).
                        queryParam(RequestInfo.QUERY_PARAMETER_TIMESTAMP, timeStamp).request();

        Response response = null;
        try {
            response = request.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + accessToken).header(
                    RequestInfo.HEADER_NFC_TOKEN_HASH, Crypto.generateTOTP(tag_password)).post(
                    Entity.entity(avgHeartRate, MediaType.APPLICATION_OCTET_STREAM));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
        }

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
