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
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String OPEN_BRACKET = " (";
    private static final String CLOSE_BRACKET = ")";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";


    private static String CREATE_TABLE_STEPS = CREATE_TABLE + StepsPersistenceContract.StepsEntry.TABLE_NAME + OPEN_BRACKET + "date INTEGER, steps INTEGER" + CLOSE_BRACKET;

    private static String CREATE_STEPS_TABLE =
            CREATE_TABLE + StepsPersistenceContract.StepsEntry.TABLE_NAME + OPEN_BRACKET +
                    StepsPersistenceContract.StepsEntry._ID + TEXT_TYPE + PRIMARY_KEY +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    StepsPersistenceContract.StepsEntry.COLUMN_NAME_COMPLETED + BOOLEAN_TYPE +
                    CLOSE_BRACKET;

    private static String CREATE_SESSION_TABLE =
            CREATE_TABLE + SessionContract.SessionEntry.TABLE_NAME + OPEN_BRACKET +
                    SessionContract.SessionEntry._ID + TEXT_TYPE + PRIMARY_KEY +
                    SessionContract.SessionEntry.COLUMN_NAME_FILENAME + TEXT_TYPE + COMMA_SEP +
                    SessionContract.SessionEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    SessionContract.SessionEntry.COLUMN_NAME_HASH + TEXT_TYPE + COMMA_SEP +
                    SessionContract.SessionEntry.COLUMN_NAME_LAST_MODIFIED + TEXT_TYPE +
                    CLOSE_BRACKET;

    protected static final AtomicInteger openCounter = new AtomicInteger();

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STEPS);
        db.execSQL(CREATE_SESSION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_IF_EXISTS + StepsPersistenceContract.StepsEntry.TABLE_NAME);
        db.execSQL(DROP_TABLE_IF_EXISTS + SessionContract.SessionEntry.TABLE_NAME);

        onCreate(db);
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
