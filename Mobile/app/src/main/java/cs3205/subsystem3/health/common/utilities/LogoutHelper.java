package cs3205.subsystem3.health.common.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;

import cs3205.subsystem3.health.MainActivity;
import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by danwen on 10/10/17.
 */

public class LogoutHelper {

    public static void logout(final Context context, final String message) {
        File sharedPreferenceFile = new File("/data/data/"+ context.getPackageName() + "/shared_prefs/");
        Log.d("LogoutHelper", "Number of shared preference files created: " + sharedPreferenceFile.listFiles().length);
        for (File file : sharedPreferenceFile.listFiles()) {
            file.delete();
        }
        Log.d("LogoutHelper", "Number of shared preference files left on logout: " + sharedPreferenceFile.listFiles().length);

        //show toast message
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

        //logout
        Intent logoutFromMainIntent = new Intent(context, MainActivity.class);
        logoutFromMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(logoutFromMainIntent);
    }
}
