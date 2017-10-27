package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.ui.heartrate.HeartRateReaderActivity;

/**
 * Created by danwen on 7/10/17.
 */

public class HeartRateUploadTask extends AsyncTask<Object, Void, Boolean> {

    private Context context;
    private Client client;

    @Override
    protected Boolean doInBackground(Object... params) {
        String tag_password = (String) params[0];
        String timeStamp = (String)params[1];
        String avgHeartRate = (String)params[2];
        context = (Context)params[3];
        client = ClientBuilder.newClient();

        if (upload(tag_password, timeStamp, avgHeartRate)) {
            client.close();
            return true;
        }

        client.close();
        return false;
    }

    private boolean upload(String tag_password, String timeStamp, String avgHeartRate) {

        String jwt = JSONWebToken.getInstance().getData();
        Invocation.Builder request = client
                .target(RequestInfo.URL_HEART_RATE_UPLOAD).
                        queryParam(RequestInfo.QUERY_PARAMETER_TIMESTAMP, timeStamp).request();

        Response response = null;
        if (!Internet.isConnected(context)) {
            makeToastMessage(AppMessage.TOAST_MESSAGE_NO_INTERNET_CONNECTION);
            return false;
        }
        try {
            response = request.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + jwt).header(
                    RequestInfo.HEADER_NFC_TOKEN_HASH, Crypto.generateNfcAuthToken(tag_password.getBytes())).post(
                    Entity.entity(avgHeartRate, MediaType.APPLICATION_OCTET_STREAM));
        } catch (CryptoException e) {
            e.printStackTrace();
            makeToastMessage(AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED);
            return false;
        } catch (RuntimeException e) {
            makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
            return false;
        }

        if (response != null && response.getStatus() == 200) {
            return true;
        } else {
            Log.d("HeartRateUploadTask", response.toString());
            return false;
        }

    }

    private void makeToastMessage(final String message) {
        ((HeartRateReaderActivity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
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
