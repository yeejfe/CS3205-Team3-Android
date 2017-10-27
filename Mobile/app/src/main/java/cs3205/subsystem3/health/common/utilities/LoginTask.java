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

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.ui.login.LoginActivity;

/**
 * Created by danwen on 6/10/17.
 */


public class LoginTask extends AsyncTask<Object, Void, Boolean> {
    private Context context;
    private byte[] challenge;
    private String salt;
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

        //handle login challenge
        if (!handleLoginChallenge()) {
            return false;
        }

        //handle formal login
        return handleFormalLogin();
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


        if (response != null && response.getStatus() == 401) {
            JSONObject headers = null;
            try {
                Log.d("LoginTask", "headers: " + response.getHeaders());
                headers = new JSONObject(response.getHeaderString("www-authenticate"));
                Log.d("LoginTask", "salt : " + headers.get(Value.KEY_VALUE_SALT) +
                        "; encoded challenge: " + headers.get(Value.KEY_VALUE_CHALLENGE));
                salt = (String) headers.get(Value.KEY_VALUE_SALT);
                challenge = Base64.decode((String) headers.get(Value.KEY_VALUE_CHALLENGE), Base64.NO_WRAP);
                Log.d("LoginTask", "decoded challenge: " + challenge +
                        "length of the challenge: " + challenge.length);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.d("LoginTask", "error status code on requesting challenge" + String.valueOf(response.getStatus()));
            Log.d("LoginTask", "response content on requesting challenge: " + response.toString());
            return false;
        }
    }

    private boolean handleFormalLogin() {

        Invocation.Builder loginRequest = client.target(RequestInfo.URL_LOGIN).request();
        String nfcTokenHash = null;
        String challengeResponse = null;
        try {
            nfcTokenHash = Crypto.generateNfcAuthToken(tag_password.getBytes());
            Log.d("LoginTask", "nfc token hash: " + nfcTokenHash);
            challengeResponse = Base64.encodeToString(Crypto.generateChallengeResponse(password + salt, challenge), Base64.NO_WRAP);
            Log.d("LoginTask", "challenge result: " + challengeResponse);
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
                    .header(RequestInfo.HEADER_NFC_TOKEN_HASH, nfcTokenHash).header("debug", "true")
                    .post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        } catch (RuntimeException e) {
            isInternetError = true;
            makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
            e.printStackTrace();
            return false;
        }

        if (response != null && response.getStatus() != 200) {
            Log.d("LoginTask", "error status code on challenge response: " + response.getStatus());
            Log.d("LoginTask", "response content aon challenge response: " + response.toString());
            return false;
        } else {
            String strResponse = response.readEntity(String.class);
            if (!strResponse.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(strResponse);
                    String accessToken = jsonResponse.get(Value.KEY_VALUE_JWT_ACCESS_TOKEN).toString();
                    JSONWebToken.getInstance().setData(accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
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
