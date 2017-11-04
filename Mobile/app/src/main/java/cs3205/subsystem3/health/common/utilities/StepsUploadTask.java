package cs3205.subsystem3.health.common.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.MainActivity;
import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.core.JSONFileReader;
import cs3205.subsystem3.health.common.crypto.Encryption;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;
import cs3205.subsystem3.health.ui.step.StepUploadFragment;

/**
 * Created by Yee on 10/21/17.
 */

public class StepsUploadTask extends AsyncTask<Object, String, Integer> {

    public static final String TITLE = "Upload Sessions";
    public static final int SLEEP_TIME = 2500;
    public static final String MESSAGE_SUCCESS = "All sessions have been uploaded successfully.";
    public static final String UPLOAD_FAILED_FOR_ALL = "Upload failed for all sessions.";
    public static final String SUCCESSFUL = "Successful";
    public static final String FAILED = "Failed";
    public static final String UPLOADING = "Uploading (";
    public static final String FRONT_SLASH = "/";
    public static final String SESSIONS = ") sessions...";
    public static final String TAB = "\t";
    public static final String SESSIONS_UPLOAD_COMPLETED = " sessions upload completed.";
    public static final String OF = " of ";
    public static final String HAVE_BEEN_UPLOADED_SUCCESSFULLY = " have been uploaded successfully.";
    public static final int UI_SLEEP_TIME = 500;

    private String TAG = this.getClass().getName();

    private Context context;
    private ProgressBar progressbar;
    private AlertDialog alertDialog;
    private StepUploadFragment frag;

    private ArrayList<String> selectedItems;
    private ArrayList<Boolean> uploadedItems;

    public StepsUploadTask(StepUploadFragment stepUploadFragment, AlertDialog alertDialog, ProgressBar progressbar) {
        this.frag = stepUploadFragment;
        this.alertDialog = alertDialog;
        this.progressbar = progressbar;
        this.uploadedItems = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Object... params) {
        String tag_password = (String) params[0];
        String timeStamp = (String) params[1];
        selectedItems = (ArrayList<String>) params[2];
        context = (Context) params[3];

        int uploaded = upload(tag_password, timeStamp, selectedItems);

        if (uploaded == selectedItems.size()) {
            publishProgress(MESSAGE_SUCCESS, new String());
        } else if(uploaded == 0) {
            publishProgress(UPLOAD_FAILED_FOR_ALL, new String());
        } else {
            publishProgress(uploaded + OF + selectedItems.size() + HAVE_BEEN_UPLOADED_SUCCESSFULLY, new String());
        }

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return uploaded;
    }

    private int upload(String tag_password, String timeStamp, ArrayList<String> selectedFiles) {
        if (!Internet.isConnected(context)) {
            makeToastMessage(AppMessage.TOAST_MESSAGE_NO_INTERNET_CONNECTION);
            return 0;
        }

        int uploaded = selectedFiles.size();

        String jwt = JSONWebToken.getInstance().getData();

        Queue<Response> responses = new LinkedList<>();

        int progress = 1;

        for (int i = 0; i < selectedFiles.size(); i++) {
            try {
                publishProgress(UPLOADING + (i + 1) + FRONT_SLASH + selectedFiles.size() + SESSIONS);
                Thread.sleep(UI_SLEEP_TIME);
                progressbar.setProgress(progress);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File file = new File(selectedFiles.get(i));

            InputStream stream = null;
            try {
                stream = new FileInputStream(file);

                byte[] contents = Encryption.getInstance().d(stream, tag_password);
                Log.d(TAG, new String(contents));

                stream = new ByteArrayInputStream(contents);
            } catch (Exception e) {
                e.printStackTrace();
            }


            Response response;

            RemoteDataSource rDS = new RemoteDataSource();
            try {
                response = rDS.buildFileUploadRequest(stream, jwt, tag_password, Long.valueOf(file.getName()), RemoteDataSource.Type.STEPS);
                responses.add(response);
            } catch (CryptoException e) {
                e.printStackTrace();
                makeToastMessage(AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED);
                return 0;
            }catch (RuntimeException e) {
                makeToastMessage(AppMessage.TOAST_MESSAGE_FAILED_CONNECTION_TO_SERVER);
                return 0;
            }
            rDS.close();

            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response != null && response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                uploadedItems.add(true);
            } else {
                uploaded--;
                uploadedItems.add(false);
            }

            try {
                String msg;
                if(uploadedItems.get(i))
                    msg = SUCCESSFUL;
                else
                    msg = FAILED;

                publishProgress(UPLOADING + (i + 1) + FRONT_SLASH + selectedFiles.size() + SESSIONS + TAB + msg);
                Thread.sleep(UI_SLEEP_TIME);
                progress++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                LogoutHelper.logout(context, AppMessage.TOAST_MESSAGE_EXPIRED_JWT);
            }
        }

        return uploaded;
    }

    private void makeToastMessage(final String message) {
        ((MainActivity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSnackBarMessage(final String message) {
        ((MainActivity)context).runOnUiThread(new Runnable() {
            public void run() {
                Snackbar.make(((MainActivity) context).findViewById(R.id.upload_fragment), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onProgressUpdate(String... objects) {
        alertDialog.setMessage(objects[0]);
        if(objects.length > 1)
            progressbar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPreExecute() {
        alertDialog.setTitle(TITLE);
    }

    @Override
    protected void onPostExecute(Integer uploaded) {
        if (uploaded > 0) {
            frag.refreshFiles(uploadedItems);
            showSnackBarMessage(uploaded + FRONT_SLASH + selectedItems.size() + SESSIONS_UPLOAD_COMPLETED);
        } else {
            showSnackBarMessage(UPLOAD_FAILED_FOR_ALL);
        }

        frag.clear();
        alertDialog.dismiss();
    }
}
