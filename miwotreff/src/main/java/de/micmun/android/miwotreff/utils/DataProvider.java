package de.micmun.android.miwotreff.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.json.JSONException;

/**
 * Content provider for the miwotreff database.
 *
 * @author Michael Munzert
 * @version 1.0, 12.08.2013
 */
public class DataProvider extends ContentProvider {
    private static final String TAG = "MiWoTreff.DataProvider";
    // IDs for matching content
    private static final int ROOT_ID = 0;
    private static final int TABLE_PROGRAM_ID = 10;
    private static final UriMatcher mUriMatcher = new UriMatcher(ROOT_ID);

    /**
     * Static constructor for the uri's to match.
     */
    static {
        mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME, TABLE_PROGRAM_ID);
    }

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        mDb = mDbHelper.getWritableDatabase();
        return true;
    }

    /**
     * @see ContentProvider#query(android.net.Uri, String[], String, String[], String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor res = null;
        switch (mUriMatcher.match(uri)) {
            case TABLE_PROGRAM_ID:
                projection = new String[]{DBConstants._ID, DBConstants.KEY_DATUM,
                        DBConstants.KEY_THEMA, DBConstants.KEY_PERSON};
                sortOrder = DBConstants.KEY_DATUM + " desc";
                res = mDb.query(DBConstants.TABLE_NAME, projection, null, null, null, null, sortOrder);
                res.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return res;
    }

    @Override
    public String getType(Uri uri) {
        String type = null;

        switch (mUriMatcher.match(uri)) {
            case TABLE_PROGRAM_ID:
                type = "vnd.android.cursor.dir/vnd." + DBConstants.AUTHORITY + "."
                        + DBConstants.TABLE_NAME;
                break;
        }

        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri res = null;

        switch (mUriMatcher.match(uri)) {
            case TABLE_PROGRAM_ID:
                long id = mDb.insert(DBConstants.TABLE_NAME, null, values);
                if (id != -1) {
                    res = uri.withAppendedPath(uri, String.valueOf(id));
                    getContext().getContentResolver().notifyChange(uri, null);
                }
        }
        return res;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Insert an entry (date, topic, person) and returns the id of the new entry.
     *
     * @param datum  date of the new entry.
     * @param thema  topic of the new entry.
     * @param person person who manage the event.
     * @return id of the new entry.
     */
//    public long createEntry(Date datum, String thema, String person) {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(DBConstants.KEY_DATUM, datum.getTime());
//        initialValues.put(DBConstants.KEY_THEMA, thema);
//        initialValues.put(DBConstants.KEY_PERSON, person);
//        return mDb.insertWithOnConflict(DBConstants.TABLE_NAME, null, initialValues,
//                SQLiteDatabase.CONFLICT_REPLACE);
//    }

    /**
     * Deletes the entry with the id from database.
     *
     * @param rowId id, which will be deleted.
     * @return <code>true</code> if entry could be deleted, else
     * <code>false</code>.
     */
//    public boolean deleteEntry(long rowId) {
//        return mDb.delete(DBConstants.TABLE_NAME, DBConstants._ID + "=" + rowId, null) > 0;
//    }

    /**
     * Updates an entry with id <code>rowId</code> with the new topic and person.
     * Returns <code>true</code>, if success.
     *
     * @param rowId  id to update.
     * @param thema  new topic.
     * @param person new person.
     * @return <code>true</code> if success, else <code>false</code>.
     */
//    public boolean updateEntry(long rowId, String thema, String person) {
//        ContentValues values = new ContentValues();
//        values.put(DBConstants.KEY_THEMA, thema);
//        values.put(DBConstants.KEY_PERSON, person);
//        return mDb.update(DBConstants.TABLE_NAME, values, DBConstants._ID + "=" + rowId, null) > 0;
//    }

    /**
     * Returns Cursor for all entries with query or all.
     *
     * @param query Query of the database entries or <code>null</code>.
     * @return Cursor of table "programm".
     */
//    public Cursor fetchAllEntries(String query) {
//        query = createQuery(query);
//        return mDb.query(DBConstants.TABLE_NAME, new String[]{DBConstants._ID, DBConstants.KEY_DATUM,
//                DBConstants.KEY_THEMA, DBConstants.KEY_PERSON}, query, null, null, null, DBConstants.KEY_DATUM
//                + " desc");
//    }

    /**
     * Creates the query from the value.
     *
     * @param q Value is a date, a topic or a person with '-'.
     * @return where-Clause for database query.
     */
//    private String createQuery(String q) {
//        String query = null;
//
//        if (q == null) {
//            // do nothing -> query is null
//        } else if (q.charAt(0) >= '0' && q.charAt(0) <= '9') {
//            Date d = getDateFromString(q);
//            query = "datum = " + d.getTime();
//        } else {
//            String s = q.toUpperCase(def);
//            String tmp = "upper(person) like '%%%s%%' or "
//                    + "upper(thema) like '%%%s%%'";
//            query = String.format(def, tmp, s, s);
//        }
//
//        return query;
//    }

    /**
     * Returns the cursor for entry with the given id.
     *
     * @param rowId id of the selected entry.
     * @return Cursor of the entry or <code>null</code>, if not found.
     * @throws android.database.SQLException if an error occurs while reading from database.
     */
//    public Cursor fetchEntry(long rowId) throws SQLException {
//        Cursor mCursor = mDb.query(DBConstants.TABLE_NAME, new String[]{DBConstants._ID,
//                DBConstants.KEY_DATUM, DBConstants.KEY_THEMA, DBConstants.KEY_PERSON},
//                DBConstants._ID + "=" + rowId, null,
//                null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//
//        return mCursor;
//    }

    /**
     * Returns the app data as json.
     *
     * @return {@link org.json.JSONArray JSONArray}
     */
//    public JSONArray getJSonData() {
//        JSONArray dataList = new JSONArray();
//        JSONObject data;
//
//        Cursor c = fetchAllEntries(null);
//
//        while (c.moveToNext()) {
//            String d = getDateString(c.getLong(1));
//            String t = c.getString(2);
//            String p = c.getString(3);
//            data = new JSONObject();
//            try {
//                data.put(DBConstants.KEY_DATUM, d);
//                data.put(DBConstants.KEY_THEMA, t);
//                data.put(DBConstants.KEY_PERSON, p);
//                dataList.put(data);
//            } catch (JSONException e) {
//                Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
//            }
//        }
//
//        return dataList;
//    }

    /**
     * Writes the JSON data in the database.
     *
     * @param data {@link org.json.JSONArray JSONArray}.
     * @throws JSONException
     */
//    public void writeJSonData(JSONArray data) throws JSONException {
//        for (int i = 0; i < data.length(); ++i) {
//            JSONObject o = data.getJSONObject(i);
//            Date d = getDateFromString(o.getString(DBConstants.KEY_DATUM));
//            String t = o.getString(DBConstants.KEY_THEMA);
//            String p = o.getString(DBConstants.KEY_PERSON);
//            createEntry(d, t, p);
//        }
//    }
}
