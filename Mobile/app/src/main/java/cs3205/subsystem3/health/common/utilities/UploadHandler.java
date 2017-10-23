package cs3205.subsystem3.health.common.utilities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;


public class UploadHandler extends AppCompatActivity {

    public static final String MESSAGE_EXCEED_MAX_SIZE = "Exceeded the maximum size: 50MB";
    public static final String MESSAGE_RESPONSE_TITLE = "Response from Servers";
    public static final String MESSAGE_SUCCESSFUL = "Successful";
    public static final String MESSAGE_FAIL = "Fail";
    public static final String MESSAGE_NFC_READ_FAIL = "NFC read fail";
    public static final String IMAGE = "IMAGE";



    private Context context;
    private String path;
    private String jwtToken;
    private String nfcToken;
    private RemoteDataSource.Type choice;


    long totalSize = 0;


    public UploadHandler(String path,RemoteDataSource.Type choice, Context context){
        this.context = context;
        this.path = path;
        this.choice = choice;

    }

    public void startUpload(){
        getJwtToken();
        getNfcToken();
    }


    private void getJwtToken(){
        jwtToken = "";
        jwtToken =  JSONWebToken.getInstance().getData();
        Log.d("UploadHandler", jwtToken);
    }


    private void getNfcToken(){
        Intent startNFCReadingActivity = new Intent(this, NFCReaderActivity.class);
        startActivityForResult(startNFCReadingActivity, 70);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("UploadHandler", "request code: " + requestCode);
        if (requestCode == 70) {
            if (resultCode == RESULT_OK) {
                nfcToken = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
                Log.d("UploadHandler", "NFC token is "+nfcToken);
                try {
                    if(choice.equals(IMAGE)) {
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
        } else {
            Log.d("NFC Read for Upload ", "request fail");
            failNfcRead();
        }
    }

    private void failNfcRead(){
        Toast.makeText(this, MESSAGE_NFC_READ_FAIL , Toast.LENGTH_SHORT).show();
    }




    private boolean upload() {
        File f = new File(path);
        long length = f.length() / (1024 * 1024);  // length is expressed in MB
        if (length < 50.00) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                RemoteDataSource rDS = new RemoteDataSource();
                Response response = rDS.buildFileUploadRequest(stream, jwtToken, nfcToken, Long.valueOf(Timestamp.getEpochTimeStamp()), choice);

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
            }
            catch(Exception e){
                e.printStackTrace();
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
        protected String doInBackground(Void... params) {
            return uploadBigFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadBigFile() {

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

                    try {

                        RemoteDataSource rDS = new RemoteDataSource();
                        Response response = rDS.buildFileUploadRequest(stream, jwtToken, nfcToken, Long.valueOf(Timestamp.getEpochTimeStamp()), choice);

                        rDS.close();
                        // Check Response

                        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {

                            responseString = MESSAGE_SUCCESSFUL;

                        } else {
                            responseString = MESSAGE_FAIL;
                        }

                    } catch (Exception e){
                        e.printStackTrace();
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
