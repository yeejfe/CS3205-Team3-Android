package cs3205.subsystem3.health.common.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.ui.login.LoginActivity;

/**
 * Created by danwen on 6/10/17.
 */


public class LoginTask extends AsyncTask<Object, Void, Boolean> {
    final static String LOGIN_URL = "https://cs3205-3.comp.nus.edu.sg/oauth/token";
    private Context context;

    @Override
    protected Boolean doInBackground(Object... params) {
        context = (Context) params[2];
        return connectToServer((String) params[0], (String) params[1], context);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onLoginSuccess or onLoginFailed
                            ((LoginActivity) context).onLoginSuccess();

                        }
                    }, 3000);
        } else {
            ((LoginActivity) context).onLoginFailed();
        }
    }

    private boolean connectToServer(String body, String tag_password, Context context) {
        Invocation.Builder request = ClientBuilder.newClient().target(LOGIN_URL).request();
        String nfcTokenHash = null;
        try {
            nfcTokenHash = Base64.encodeToString(HashGenerator.generateHash(tag_password), Base64.URL_SAFE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        Response response = request.header(RequestInfo.HEADER_NFC_TOKEN_HASH, nfcTokenHash).post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200) {
            return false;
        } else {
            String strResponse = response.readEntity(String.class);
            if (!strResponse.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(strResponse);
                    String accessToken = jsonResponse.get(Value.KEY_VALUE_JWT_ACCESS_TOKEN).toString();
                    Long timestamp = System.currentTimeMillis();

                    SharedPreferences savedSession = context.getSharedPreferences(Value.KEY_VALUE_SHARED_PREFERENCE_TOKEN, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = savedSession.edit();
                    editor.putString(Value.KEY_VALUE_SHARED_PREFERENCE_ACCESS_TOKEN, accessToken);
                    editor.putString(Value.KEY_VALUE_SHARED_PREFERENCE_NFC_TOKEN_HASH,nfcTokenHash);
                    editor.putLong(Value.KEY_VALUE_SHARED_PREFERENCE_TIMESTAMP, timestamp);
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }
}
