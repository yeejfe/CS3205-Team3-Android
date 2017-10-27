package cs3205.subsystem3.health.common.utilities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;



public class UploadHandler{


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


    public UploadHandler(String path,RemoteDataSource.Type choice, Context context,String jwtToken, String nfcToken){
        this.context = context;
        this.path = path;
        this.choice = choice;
        this.jwtToken = jwtToken;
        this.nfcToken = nfcToken;

    }

    public void startUpload(){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
