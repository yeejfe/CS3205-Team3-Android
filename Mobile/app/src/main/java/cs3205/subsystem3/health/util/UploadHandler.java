package cs3205.subsystem3.health.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;

import static cs3205.subsystem3.health.ui.camera.UploadPageActivity.MESSAGE_FAIL;
import static cs3205.subsystem3.health.ui.camera.UploadPageActivity.MESSAGE_RESPONSE_TITLE;
import static cs3205.subsystem3.health.ui.camera.UploadPageActivity.MESSAGE_SUCCESSFUL;

public class UploadHandler extends AppCompatActivity {

    TextView textView;
    String path;
    String token;
    String hash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_handler);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        textView = (TextView) findViewById(R.id.textView2);
        getToken();
        getNfcHash();
        upload();

    }



        private void getToken(){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            token = pref.getString("access_token", "");
            textView.setText(token);
            System.out.println("token is "+ token);
        }
    private String getNfcHash(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        hash = pref.getString("nfc_hash", "");
        return hash;
    }




        public boolean upload() {
            getToken();
            File f = new File(path);
            long length = f.length() / (1024 * 1024);  // length is expressed in MB
            if (length < 10.00) {
                InputStream stream = null;
                try {
                    stream = new FileInputStream(f);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RemoteDataSource rDS = new RemoteDataSource();
                Response response = rDS.buildFileUploadRequest(stream, token, getNfcHash());

                rDS.close();
                // Check Response

                if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                    showAlert(MESSAGE_SUCCESSFUL);
                    return true;

                } else {
                    showAlert(MESSAGE_FAIL);
                    return false;
                }
            } else {
                showAlert(MESSAGE_FAIL);
                return false;
            }



        }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(MESSAGE_RESPONSE_TITLE)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }






}
