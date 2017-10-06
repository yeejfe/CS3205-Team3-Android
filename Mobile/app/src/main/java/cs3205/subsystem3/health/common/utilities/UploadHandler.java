package cs3205.subsystem3.health.common.utilities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;


public class UploadHandler extends AppCompatActivity {

    public static final String MESSAGE_EXCEED_MAX_SIZE = "Exceeded the maximum size: 50MB";
    public static final String MESSAGE_RESPONSE_TITLE = "Response from Servers";
    public static final String MESSAGE_SUCCESSFUL = "Successful";
    public static final String MESSAGE_FAIL = "Fail";

    private TextView textView;
    private ProgressBar progressBar;
    private TextView txtPercentage;


    private String path;
    private String token;
    private String hash;
    private String choice;

    long totalSize = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_upload_handler);
            Intent intent = getIntent();
            path = intent.getStringExtra("path");
            choice = intent.getStringExtra("choice");
            textView = (TextView) findViewById(R.id.textView2);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            txtPercentage = (TextView) findViewById(R.id.txtPercentage);

          //     getSupportActionBar().setBackgroundDrawable(
          //             new ColorDrawable(Color.parseColor(getResources().getString(
           //                    0+R.color.action_bar))));

        }
        catch (Exception ex){
            ex.printStackTrace();
        }



        getToken();
        getNfcHash();
        try {
            if(choice.equals("image")) {
                upload();
            }
            else{
                new UploadAsync().execute();

            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }



    private void getToken(){
        SharedPreferences pref = getSharedPreferences("Token_SharedPreferences", Activity.MODE_PRIVATE);
        token = pref.getString("access_token", "");
        textView.setText(token);
        System.out.println("token is "+ token);
    }


    private String getNfcHash(){
        SharedPreferences pref = getSharedPreferences("Token_SharedPreferences", Activity.MODE_PRIVATE);
        hash = pref.getString("nfc_hash", "");
        System.out.println("hash is "+ hash);
        return hash;
    }




    public boolean upload() {
        File f = new File(path);
        long length = f.length() / (1024 * 1024);  // length is expressed in MB
        if (length < 50.00) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RemoteDataSource rDS = new RemoteDataSource();
            Response response = rDS.buildFileUploadRequest(stream, token, getNfcHash(),choice);

            rDS.close();
            // Check Response

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                showAlert(MESSAGE_SUCCESSFUL);
                return true;

            } else {
                Log.e("connection", "response is " + response.readEntity(String.class));
                showAlert(MESSAGE_FAIL);
                return false;
            }
        } else {
            showAlert(MESSAGE_EXCEED_MAX_SIZE);
            return false;
        }

    }

    /**
     * Uploading the file to server
     * */
    private class UploadAsync extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadBigFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadBigFile() {
           /* AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });*/

                String responseString = null;
                File f = new File(path);
                long length = f.length() / (1024 * 1024);  // length is expressed in MB
                if (length < 50.00) {
                    InputStream stream = null;
                    try {
                        stream = new FileInputStream(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RemoteDataSource rDS = new RemoteDataSource();
                    Response response = rDS.buildFileUploadRequest(stream, token, getNfcHash(), choice);

                    rDS.close();
                    // Check Response

                    if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {

                        responseString = MESSAGE_SUCCESSFUL;

                    } else {
                        responseString = MESSAGE_FAIL;
                    }
                } else {
                    responseString = MESSAGE_EXCEED_MAX_SIZE;
                }

                return responseString;

        }


        @Override
        protected void onPostExecute(String result) {
            showAlert(result);
            super.onPostExecute(result);
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
