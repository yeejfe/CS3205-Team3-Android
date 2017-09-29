package cs3205.subsystem3.health.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by Yee on 09/17/17.
 */

public class StepsPersistenceContract {
    private StepsPersistenceContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class StepsEntry implements BaseColumns {
        public static final String TABLE_NAME = "steps";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_COMPLETED = "completed";
    }
}