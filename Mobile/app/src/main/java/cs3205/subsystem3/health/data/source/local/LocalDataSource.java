package cs3205.subsystem3.health.data.source.local;

/**
 * Created by Yee on 09/17/17.
 */

import android.content.Context;

public class LocalDataSource {
    private static Database instance;

    private LocalDataSource(final Context context) {
        instance = new Database(context.getApplicationContext());
    }

    public static synchronized Database getInstance(final Context c) {
        if (instance == null) {
            instance = new Database(c.getApplicationContext());
        }
        instance.openCounter.incrementAndGet();
        return instance;
    }


}
