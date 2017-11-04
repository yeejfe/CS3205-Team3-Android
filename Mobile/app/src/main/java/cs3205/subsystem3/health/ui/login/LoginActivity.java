package cs3205.subsystem3.health.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.LoginTask;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.logic.session.Timeout;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;


/**
 * Created by Yee on 09/30/17.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();
    private static final boolean NO_INTERNET_ERROR = false;

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    private TextView mLoginTimer;
    private String username;
    private String password;
    private String tag_username;
    private String tag_password;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        _usernameText = (EditText) findViewById(R.id.input_username);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        mLoginTimer = (TextView) findViewById(R.id.login_timer);

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

        hideKeyboardAndFocus();

        if (!validate(username, password)) {
            Timeout.getInstance().setDuration(Timeout.DEFAULT_TIMEOUT_IN_SECONDS);
            onLoginFailed(NO_INTERNET_ERROR);
            return;
        }

        _loginButton.setEnabled(false);

        progressBar = new ProgressBar(LoginActivity.this, null, R.style.AppTheme_Dark_Dialog);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        authenticate();
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        Timeout.getInstance().reset();
        progressBar.setVisibility(View.GONE);

        Intent resultData = new Intent();
        resultData.putExtra(Value.KEY_VALUE_LOGIN_INTENT_USERNAME, tag_username);
        setResult(Activity.RESULT_OK, resultData);

        finish();
    }

    public void onLoginFailed(boolean isInternetError) {
        //if no internet connection, not considered as login failure
        if (isInternetError) {
            _loginButton.setEnabled(true);
            return;
        }
        _loginButton.setEnabled(false);
        showSnackBarMessage(AppMessage.TOAST_MESSAGE_LOGIN_FAILURE);

        //TODO: set this value based on number of login failures so far
        final int loginDelayMillis = Timeout.getInstance().getDuration();
        Log.d(TAG, "Retry in " + loginDelayMillis / 1000);
        new Handler().postAtTime(//enable the login button after the time delay and display the count down to user
                new Runnable() {
                    public void run() {
                        mLoginTimer.setVisibility(View.VISIBLE);
                        new CountDownTimer(loginDelayMillis, 1000) {

                            public void onTick(long millisUntilFinished) {
                                mLoginTimer.setText("you can retry in " + millisUntilFinished / 1000 + " seconds");
                            }

                            public void onFinish() {
                                mLoginTimer.setVisibility(View.INVISIBLE);
                                _loginButton.setEnabled(true);
                            }
                        }.start();
                    }
                }, loginDelayMillis);
        if (SessionManager.isLogoutTimerSet()) {
            SessionManager.cancelLogoutTimer();
        }
    }

    public boolean validate(String username, String password) {

        if (username.isEmpty()) {
            _usernameText.setError(AppMessage.ERROR_MESSAGE_EMPTY_USERNAME);
            return false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8 || password.length() > 20) {
            _passwordText.setError(AppMessage.ERROR_MESSAGE_INVALID_PASSWORD_LENGTH);
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
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 30) {
            if (resultCode == RESULT_OK) {
                tag_username = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_USERNAME);
                tag_password = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
            }
            if (tag_password == null || tag_username == null) {
                manageTimeout();
                onLoginFailed(NO_INTERNET_ERROR);
            } else if (!tag_username.equals(username)) {
                manageTimeout();
                onLoginFailed(NO_INTERNET_ERROR);
            } else {
                JSONObject body = new JSONObject();
                try {
                    body.put(RequestInfo.HEADER_GRANT_TYPE, RequestInfo.GRANT_TYPE_PASSWORD);
                    body.put(RequestInfo.HEADER_USERNAME, username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new LoginTask().execute(body, password, tag_password, this);
            }
        }

    }

    public void hideKeyboardAndFocus() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            _usernameText.clearFocus();
            _passwordText.clearFocus();
        }
    }

    private void manageTimeout() {
        int count = Timeout.getInstance().getCount();
        if (count > 0) {
            double duration = Math.pow(Timeout.DEFAULT_TIMEOUT_IN_SECONDS, count);
            if (duration > Integer.MAX_VALUE) {
                duration = Integer.MAX_VALUE;
            }
            Timeout.getInstance().setDuration((int) duration);
        }
    }
}
