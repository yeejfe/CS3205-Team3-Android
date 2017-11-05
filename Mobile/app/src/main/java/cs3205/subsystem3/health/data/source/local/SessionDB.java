package cs3205.subsystem3.health.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.model.Session;

/**
 * Created by Yee on 10/05/17.
 */

public class SessionDB implements DbHelper {
    private String TAG = this.getClass().getName();

    private Database db;

    private String TABLE_NAME = SessionContract.SessionEntry.TABLE_NAME;

    public SessionDB(Context context) {
        db = LocalDataSource.getInstance(context);
    }

    public void close() {
        db.close();
    }

    public long insertSession(Session session) {
        db.getWritableDatabase().beginTransaction();

        long id;

        try {
            ContentValues values = new ContentValues();
            values.put(SessionContract.SessionEntry.COLUMN_NAME_TITLE, session.getTitle());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_FILENAME, session.getFilename());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_HASH, session.getHash());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_LAST_MODIFIED, session.getLastModified());

            // insert row
            id = db.getWritableDatabase().insert(TABLE_NAME, null, values);

            db.getWritableDatabase().setTransactionSuccessful();
        } finally {
            db.getWritableDatabase().endTransaction();
        }
        return id;
    }

    public Session getSession(String fileName) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                SessionContract.SessionEntry.COLUMN_NAME_FILENAME + "='" + fileName + "'";

        Cursor c = db.getReadableDatabase().rawQuery(query, null);
        if (c != null)
            c.moveToFirst();

        Session session = new Session(c.getString(c.getColumnIndex(SessionContract.SessionEntry.COLUMN_NAME_TITLE)),
                c.getString(c.getColumnIndex(SessionContract.SessionEntry.COLUMN_NAME_FILENAME)),
                c.getString(c.getColumnIndex(SessionContract.SessionEntry.COLUMN_NAME_HASH)),
                c.getString(c.getColumnIndex(SessionContract.SessionEntry.COLUMN_NAME_LAST_MODIFIED)));
        c.close();

        return session;
    }

    public void deleteSession(String fileName) {
        db.getWritableDatabase().delete(TABLE_NAME, SessionContract.SessionEntry.COLUMN_NAME_FILENAME + " = ?",
                new String[] { String.valueOf(fileName) });
    }
}
