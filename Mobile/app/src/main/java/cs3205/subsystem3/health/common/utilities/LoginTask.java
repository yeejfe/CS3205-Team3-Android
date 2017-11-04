package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.crypto.Encryption;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.logic.session.Timeout;
import cs3205.subsystem3.health.ui.login.LoginActivity;

/**
 * Created by danwen on 6/10/17.
 */


public class LoginTask extends AsyncTask<Object, Void, Boolean> {
    private Context context;
    private byte[] password_challenge;
    private String password_salt;
    private byte[] nfc_challenge;
    private Client client;
    private JSONObject body;
    private String password;
    private String tag_password;
    private boolean isInternetError;

    @Override
    protected Boolean doInBackground(Object... params) {
        body = (JSONObject) params[0];
        password = (String) params[1];
        tag_password = (String) params[2];
        context = (Context) params[3];
        client = ClientBuilder.newClient();
        return connectToServer();
    }

    @Override
    protected void onPostExecute(Boolean isLoginSuccessful) {
        client.close();

        Encryption.getInstance().setKey(tag_password);

        if (isLoginSuccessful) {
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            ((LoginActivity) context).onLoginSuccess();
                        }
                    }, 3000);
        } else {
            ((LoginActivity) context).onLoginFailed(isInternetError);
        }
    }

    private boolean connectToServer() {
        return handleLoginChallenge() && handleFormalLogin();
    }

    private boolean handleLoginChallenge() {

        Invocation.Builder LoginChallengeRequest = client.target(RequestInfo.URL_LOGIN).request();

        if (!Internet.isConnected(context)) {
            isInternetError = true;
            makeToastMessage(AppMessage.TOAST_MESSAGE_NO_INTERNET_CONNECTION);
            return false;
        }

        Response response = null;
        try {
            response = LoginChallengeRequest.post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        } catch (RuntimeException e) {
            isInternetError = true;
            makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
            e.printStackTrace();
            return false;
        }


        if (response != null && response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            JSONObject passwordHeader = null;
            try {
                Log.d("LoginTask", "headers: " + response.getHeaders());
                passwordHeader = new JSONObject(response.getHeaderString(RequestInfo.HEADER_AUTHENTICATE));
                nfc_challenge = Base64.decode(response.getHeaderString(RequestInfo.HEADER_NFC_CHALLENGE), Base64.NO_WRAP);
                Log.d("LoginTask", "salt: " + passwordHeader.get(Value.KEY_VALUE_SALT) +
                        "; encoded challenge: " + passwordHeader.get(Value.KEY_VALUE_CHALLENGE));
                Log.d("LoginTask", "decoded nfc challenge: " + response.getHeaderString(RequestInfo.HEADER_NFC_CHALLENGE));
                password_salt = (String) passwordHeader.get(Value.KEY_VALUE_SALT);
                password_challenge = Base64.decode((String) passwordHeader.get(Value.KEY_VALUE_CHALLENGE), Base64.NO_WRAP);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.d("LoginTask", "error status code on requesting challenge: " + String.valueOf(response.getStatus()));
            Log.d("LoginTask", "response content on requesting challenge: " + response.readEntity(String.class));
            return false;
        }
    }

    private boolean handleFormalLogin() {

        Invocation.Builder loginRequest = client.target(RequestInfo.URL_LOGIN).request();
        String nfcResponse = null;
        String challengeResponse = null;
        try {
            nfcResponse = Base64.encodeToString(Crypto.generateNfcResponse(tag_password, nfc_challenge), Base64.NO_WRAP);
            Log.d("LoginTask", "nfc token: " + tag_password);
            Log.d("LoginTask", "nfc token hash: " + Base64.encodeToString(Crypto.generateHash(Base64.decode(tag_password, Base64.NO_WRAP)), Base64.NO_WRAP));
            Log.d("LoginTask", "nfc response: " + nfcResponse);
            challengeResponse = Base64.encodeToString(Crypto.generatePasswordResponse(
                    password + password_salt, password_challenge), Base64.NO_WRAP);
            Log.d("LoginTask", "password response: " + challengeResponse);
        } catch (CryptoException e) {
            e.printStackTrace();
            return false;
        }

        if (!Internet.isConnected(context)) {
            isInternetError = true;
            makeToastMessage(AppMessage.TOAST_MESSAGE_NO_INTERNET_CONNECTION);
            return false;
        }

        Response response = null;
        try {
            response = loginRequest.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.CHALLENGE_RESPONSE_PREFIX + challengeResponse)
                    .header(RequestInfo.HEADER_NFC_RESPONSE, nfcResponse)
                    .post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        } catch (RuntimeException e) {
            isInternetError = true;
            makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
            e.printStackTrace();
            return false;
        }

        if (response == null || response.getStatus() != Response.Status.OK.getStatusCode()) {
            Log.d("LoginTask", "error status code on challenge response: " + response.getStatus());
            Log.d("LoginTask", "response content on challenge response: " + response.readEntity(String.class));

            if(response.getHeaderString("X-Timeout") != null){
                int timeout = Integer.parseInt(response.getHeaderString("X-Timeout"));
                Timeout.getInstance().setDuration(timeout);
                Log.d("LoginTask", "Timeout time: " + timeout);
            }

            return false;
        } else {
            JSONWebToken.getInstance().setData(response.getHeaderString(RequestInfo.HEADER_REFRESHED_JWT));
            Log.d("LoginTask", "jwt: " + response.getHeaderString(RequestInfo.HEADER_REFRESHED_JWT));

            return true;
        }
    }

    private void makeToastMessage(final String message) {
        ((LoginActivity) context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
