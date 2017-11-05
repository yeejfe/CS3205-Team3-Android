package cs3205.subsystem3.health.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by Yee on 10/05/17.
 */

public final class SessionContract {
    private SessionContract() {}

    /* Inner class that defines the table contents */
    public static class SessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "session";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_FILENAME = "filename";
        public static final String COLUMN_NAME_HASH = "hash";
        public static final String COLUMN_NAME_LAST_MODIFIED = "lastModified";
    }
}
