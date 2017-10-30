package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
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
        Invocation.Builder challengeRequest = client.target(RequestInfo.URL_HEART_RATE_UPLOAD).request();
        Response challengeResponse = null;
        if (!Internet.isConnected(context)) {
            makeToastMessage(AppMessage.TOAST_MESSAGE_NO_INTERNET_CONNECTION);
            return false;
        }
        challengeResponse = challengeRequest.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + jwt).post(null);
        Log.d("HeartRateUploadTask", "challenge response:  " + challengeResponse.readEntity(String.class));
        byte[] nfcChallenge = null;
        if (challengeResponse.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            nfcChallenge = Base64.decode(challengeResponse.getHeaderString(RequestInfo.HEADER_NFC_CHALLENGE), Base64.NO_WRAP);
            Log.d("HeartRateUploadTask", "nfc challenge: " + challengeResponse.getHeaderString(RequestInfo.HEADER_NFC_CHALLENGE));
            String newJwToken = challengeResponse.getHeaderString(RequestInfo.HEADER_REFRESHED_JWT);
            Log.d("HeartRateUploadTask", "new jwt: " + newJwToken);
            JSONWebToken.getInstance().setData(newJwToken);
        } else {
            return false;
        }


        Invocation.Builder uploadRequest = client
                .target(RequestInfo.URL_HEART_RATE_UPLOAD).
                        queryParam(RequestInfo.QUERY_PARAMETER_TIMESTAMP, timeStamp).request();
        Response uploadResponse = null;
        try {
            uploadResponse = uploadRequest.header(RequestInfo.HEADER_AUTHORIZATION,
                    RequestInfo.JWT_TOKEN_PREFIX + JSONWebToken.getInstance().getData()).
                    header(RequestInfo.HEADER_NFC_RESPONSE, Base64.encodeToString(Crypto.generateNfcResponse(tag_password,
                            nfcChallenge), Base64.NO_WRAP)).post(Entity.entity(avgHeartRate, MediaType.APPLICATION_OCTET_STREAM));
        } catch (CryptoException e) {
            e.printStackTrace();
            makeToastMessage(AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED);
            return false;
        } catch (RuntimeException e) {
            e.printStackTrace();
            makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
            return false;
        }

        if (uploadResponse != null && uploadResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
            return true;
        } else {
            Log.d("HeartRateUploadTask", "upload response: " + uploadResponse.readEntity(String.class));
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
