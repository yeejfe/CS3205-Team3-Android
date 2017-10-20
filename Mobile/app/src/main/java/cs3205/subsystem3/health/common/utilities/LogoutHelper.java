package cs3205.subsystem3.health.common.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cs3205.subsystem3.health.MainActivity;

/**
 * Created by danwen on 10/10/17.
 */

public class LogoutHelper {

    public static void logout(final Context context, final String message) {
        //TODO: clean up

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
