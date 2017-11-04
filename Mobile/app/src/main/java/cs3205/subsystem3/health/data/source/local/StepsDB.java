package cs3205.subsystem3.health.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;

/**
 * Created by Yee on 09/29/17.
 */

public class StepsDB implements DbHelper {
    private String TAG = this.getClass().getName();
    private Database db;

    private String TABLE_NAME = StepsPersistenceContract.StepsEntry.TABLE_NAME;

    public StepsDB(Context context) {
        db = LocalDataSource.getInstance(context);
    }

    public void close() {
        db.close();
    }

    public Cursor query(final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return db.getReadableDatabase()
                .query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public void insertNewDay(long date, int steps) {
        db.getWritableDatabase().beginTransaction();
        try {
            Cursor c = db.getReadableDatabase().query(TABLE_NAME, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {

                // add 'steps' to yesterdays count
                addToLastEntry(steps);

                // add today
                ContentValues values = new ContentValues();
                values.put("date", date);
                // use the negative steps as offset
                values.put("steps", -steps);
                db.getWritableDatabase().insert(TABLE_NAME, null, values);
            }
            c.close();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "insertDay " + date + " / " + steps);
            }
            db.getWritableDatabase().setTransactionSuccessful();
        } finally {
            db.getWritableDatabase().endTransaction();
        }
    }

    public void addToLastEntry(int steps) {
        if (steps > 0) {
            db.getWritableDatabase().execSQL("UPDATE " + TABLE_NAME + " SET steps = steps + " + steps +
                    " WHERE date = (SELECT MAX(date) FROM " + TABLE_NAME + ")");
        }
    }

    public boolean insertDayFromBackup(long date, int steps) {
        db.getWritableDatabase().beginTransaction();
        boolean newEntryCreated = false;
        try {
            ContentValues values = new ContentValues();
            values.put("steps", steps);
            int updatedRows = db.getWritableDatabase()
                    .update(TABLE_NAME, values, "date = ?", new String[]{String.valueOf(date)});
            if (updatedRows == 0) {
                values.put("date", date);
                db.getWritableDatabase().insert(TABLE_NAME, null, values);
                newEntryCreated = true;
            }
            db.getWritableDatabase().setTransactionSuccessful();
        } finally {
            db.getWritableDatabase().endTransaction();
        }
        return newEntryCreated;
    }

    public int getTotalWithoutToday() {
        Cursor c = db.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(Timestamp.getToday())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    public int getRecord() {
        Cursor c = db.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"MAX(steps)"}, "date > 0", null, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    public int getSteps(final long date) {
        Cursor c = db.getReadableDatabase().query(TABLE_NAME, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    public List<Pair<Long, Integer>> getLastEntries(int num) {
        Cursor c = db.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"date", "steps"}, "date > 0", null, null, null,
                        "date DESC", String.valueOf(num));
        int max = c.getCount();
        List<Pair<Long, Integer>> result = new ArrayList<>(max);
        if (c.moveToFirst()) {
            do {
                result.add(new Pair<>(c.getLong(0), c.getInt(1)));
            } while (c.moveToNext());
        }
        return result;
    }

    public int getSteps(final long start, final long end) {
        Cursor c = db.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"SUM(steps)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(start), String.valueOf(end)}, null, null, null);
        int re;
        if (c.getCount() == 0) {
            re = 0;
        } else {
            c.moveToFirst();
            re = c.getInt(0);
        }
        c.close();
        return re;
    }

    public void removeNegativeEntries() {
        db.getWritableDatabase().delete(TABLE_NAME, "steps < ?", new String[]{"0"});
    }

    public void removeInvalidEntries() {
        db.getWritableDatabase().delete(TABLE_NAME, "steps >= ?", new String[]{"200000"});
    }

    public int getDaysWithoutToday() {
        Cursor c = db.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"COUNT(*)"}, "steps > ? AND date < ? AND date > 0",
                        new String[]{String.valueOf(0), String.valueOf(Timestamp.getToday())}, null,
                        null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re < 0 ? 0 : re;
    }

    public int getDays() {
        // todays is not counted yet
        int re = this.getDaysWithoutToday() + 1;
        return re;
    }

    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        if (db.getWritableDatabase().update(TABLE_NAME, values, "date = -1", null) == 0) {
            values.put("date", -1);
            db.getWritableDatabase().insert(TABLE_NAME, null, values);
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "saving steps in db: " + steps);
        }
    }

    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }
}
