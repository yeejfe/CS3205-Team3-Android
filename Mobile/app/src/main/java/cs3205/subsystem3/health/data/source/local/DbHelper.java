package cs3205.subsystem3.health.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yee on 09/17/17.
 */

public class DbHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Health.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String BOOLEAN_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StepsPersistenceContract.StepsEntry.TABLE_NAME + " (" +
                    StepsPersistenceContract.StepsEntry._ID + TEXT_TYPE + " PRIMARY KEY," +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_COMPLETED + BOOLEAN_TYPE +
                    " )";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
