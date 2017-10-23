package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;

/**
 * Created by Yee on 10/21/17.
 */

public class StepsUploadTask extends AsyncTask<Object, Void, Boolean> {

    private String TAG = this.getClass().getName();

    private Context context;

    @Override
    protected Boolean doInBackground(Object... params) {
        String tag_password = (String) params[0];
        String timeStamp = (String) params[1];
        ArrayList<String> selectedItems = (ArrayList<String>) params[2];
        context = (Context) params[3];
        if (upload(tag_password, timeStamp, selectedItems)) {
            return true;
        }
        return false;
    }

    private boolean upload(String tag_password, String timeStamp, ArrayList<String> selectedFiles) {
        boolean status = false;

        String jwt = JSONWebToken.getInstance().getData();
        Log.d(TAG, "JWT: " + jwt);

        Queue<Response> responses = new LinkedList<>();
        ArrayList<Boolean> statuses = new ArrayList<Boolean>();

        for (int i = 0; i < selectedFiles.size(); i++) {
            File file = new File(selectedFiles.get(i));

            InputStream stream = null;
            try {
                stream = new FileInputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Response response = null;

            RemoteDataSource rDS = new RemoteDataSource();
            Log.i(TAG, rDS.toString());
            try {
                response = rDS.buildFileUploadRequest(stream, jwt, tag_password, Long.valueOf(file.getName()), RemoteDataSource.Type.STEPS);
                responses.add(response);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_AUTHENTICATION_FAILED, Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, rDS.toString());
            String body = response.readEntity(String.class);
            rDS.close();

            if (response != null && response.getStatus() == 200) {
                statuses.add(true);
                status = true;
            } else {
                Log.d(TAG, body);
                statuses.add(false);
            }
        }

        return status;
    }

    @Override
    protected void onPostExecute(Boolean isUploadSuccess) {
        if (isUploadSuccess) {
            //((HeartRateReaderActivity) context).clear();
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_SUCCESS, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, AppMessage.TOAST_MESSAGE_UPLOAD_FAILURE, Toast.LENGTH_LONG).show();
        }
    }
}
