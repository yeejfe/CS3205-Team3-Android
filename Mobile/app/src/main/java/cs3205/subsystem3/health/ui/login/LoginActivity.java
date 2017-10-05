package cs3205.subsystem3.health.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;


/**
 * Created by Yee on 09/30/17.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    private String username;
    private String password;
    private String tag_username;
    private String tag_password;
    private ProgressBar progressBar;

    final static String LOGIN_URL = "https://cs3205-3.comp.nus.edu.sg/oauth/token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setContentView(R.layout.activity_login);

        _usernameText = (EditText) findViewById(R.id.input_username);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        username = _usernameText.getText().toString();
        password = _passwordText.getText().toString();

        if (!validate(username, password)) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressBar = new ProgressBar(LoginActivity.this, null, R.style.AppTheme_Dark_Dialog);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        // TODO: Implement/call authentication logic here.
         authenticate();

    }


    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate(String username, String password) {

        if (username.isEmpty()) {
            _usernameText.setError("Username must not be empty");
            return false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8 || password.length() > 20) {
            _passwordText.setError("between 8 and 20 alphanumeric characters");
            return false;
        } else {
            _passwordText.setError(null);
        }
        return true;
    }

    private void authenticate() {
        Intent startNFCReadingActivity = new Intent(this, NFCReaderActivity.class);
        startActivityForResult(startNFCReadingActivity, 30);
    }

    private void showSnackBarMessage(String message) {
        View view = findViewById(R.id.login_activity);
        if (view != null) {
            Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 30) {
            if(resultCode == RESULT_OK) {
                tag_username = data.getStringExtra("username");
                tag_password = data.getStringExtra("password");
            }
            if (tag_password == null || tag_username == null) {
                onLoginFailed();
            } else if (!tag_username.equals(username)) {
                onLoginFailed();
            } else {
                JSONObject body = new JSONObject();
                try {
                    body.put("grant_type", "password");
                    body.put("username", "1");
                    body.put("passhash", "hash");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new LoginTask().execute(body.toString());
            }
        }

    }


    private boolean connectToServer(String body) {
        //TODO: register a JSON reader and writer
        Invocation.Builder request = ClientBuilder.newClient().target(LOGIN_URL).request();

        Response response = request.header("x-nfc-token", "hash").post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        Log.d("error", response.toString());
        if (response.getStatus() != 200) {
            Log.d("error", response.readEntity(String.class));
            return false;
        } else {
            String strResponse = response.readEntity(String.class);
            if (!strResponse.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(strResponse);
                    String accessToken = jsonResponse.get("access_token").toString();
                    Log.d("access token", accessToken);
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor  =
                            pref.edit();
                    editor.putString("access_token",accessToken);
                    editor.putString("nfc_hash","hash");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    class LoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return connectToServer(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed
                                onLoginSuccess();
                                // onLoginFailed();
                                progressBar.setVisibility(View.GONE);
                            }
                        }, 3000);
            } else {
                onLoginFailed();
            }
        }
    }

}
