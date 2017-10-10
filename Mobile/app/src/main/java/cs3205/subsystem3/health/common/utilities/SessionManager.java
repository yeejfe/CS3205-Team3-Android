package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import cs3205.subsystem3.health.common.miscellaneous.Value;

/**
 * Created by danwen on 10/10/17.
 */

public class SessionManager {
    final static long EXPIRATION_TIME = 1;

    public static boolean isSessionValid(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences
                (Value.KEY_VALUE_SHARED_PREFERENCE_TOKEN, Context.MODE_PRIVATE);
        long timestamp = sharedpreferences.getLong(Value.KEY_VALUE_SHARED_PREFERENCE_TIMESTAMP, 0);
        long currTimestamp = System.currentTimeMillis();
        long expirationTimeInMilliSeconds = TimeUnit.MINUTES.toMillis(EXPIRATION_TIME);

        return (currTimestamp - timestamp) <= expirationTimeInMilliSeconds;
    }
}
