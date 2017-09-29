package cs3205.subsystem3.health.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yee on 09/17/17.
 */

public class Database extends SQLiteOpenHelper implements DbHelper {
    public static final int DATABASE_VERSION = 1;

    private static String CREATE_TABLE_STEPS = "CREATE TABLE " + StepsPersistenceContract.StepsEntry.TABLE_NAME + " (date INTEGER, steps INTEGER)";

    private static String CREATE_STEPS_TABLE =
            "CREATE TABLE " + StepsPersistenceContract.StepsEntry.TABLE_NAME + " (" +
                    StepsPersistenceContract.StepsEntry._ID + TEXT_TYPE + " PRIMARY KEY," +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_COMPLETED + BOOLEAN_TYPE +
                    " )";

    protected static final AtomicInteger openCounter = new AtomicInteger();

    private static final String SQL_CREATE_TABLES = CREATE_TABLE_STEPS;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }
}
