/**
 * Copyright 2013 MicMun
 * 
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU >General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or >
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see >http://www.gnu.org/licenses/.
 */
package de.micmun.android.miwotreff.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Handles the database.
 * Creates the database with the table and handles the CRUD-Methods
 * (Create, Update, Delete) and the selections.
 *
 * @author MicMun
 * @version 2.0, 18.01.2013
 */
public class DbAdapter
{
   /**
    * Key for primary Key <code>_id</code>.
    */
   public static final String KEY_ROWID = "_id";
   /**
    * Key for column <code>datum</code> (date).
    */
   public static final String KEY_DATUM = "datum";
   /**
    * Key for column <code>thema</code> (topic).
    */
   public static final String KEY_THEMA = "thema";
   /**
    * Key for column <code>person</code> (person).
    */
   public static final String KEY_PERSON = "person";
   
   private static final String TAG = "DbAdapter";
   
   private static final String DATABASE_NAME = "miwotreff";
   private static final String DATABASE_TABLE = "programm";
   private static final int DATABASE_VERSION = 1;
   
   /* Create Statement for table "programm" */
   private static final String TABLE_CREATE = 
   "CREATE TABLE programm (_id integer primary key autoincrement," +
   " datum integer not null unique, thema text not null, person text);";
   
   /* Locale */
   private static final Locale def = Locale.getDefault();
   
   // Database and Context
   private SQLiteDatabase mDb; // Database SQLITE
   private DatabaseHelper mDbHelper;
   private final Context mCtx; // Context for Database
   
   /**
    * Creates the database and the table.
    *
    * @author Michael Munzert
    * @version 1.0, 11.08.2012
    */
   private static class DatabaseHelper extends SQLiteOpenHelper 
   {
      /**
       * Creates a new DatabaseHelper with a context.
       * 
       * @param  context
       *         Context of the database.
       */
      DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }
      
      /**
       * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
       */
      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL(TABLE_CREATE);
      }
      
      /**
       * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
       */
      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Evtl. Upgrades der Database
      }
   };
   
   /**
    * Creates a new DbAdapter with a context for the database.
    * 
    * @param  ctx
    *         Context for the database.
    */
   public DbAdapter(Context ctx) {
      this.mCtx = ctx;
   }
   
   /**
    * Opens the database connection and returns a reference of himself.
    * 
    * @return self-reference.
    * @throws SQLException
    *         if database connection can't be established.
    */
   public DbAdapter open() throws SQLException {
      mDbHelper = new DatabaseHelper(mCtx);
      mDb = mDbHelper.getWritableDatabase();
      return this;
   }
   
   /**
    * Closes the connection to the database.
    */
   public void close() {
      mDbHelper.close();
   }
   
   /**
    * Insert an entry (date, topic, person) and returns the id of the new entry.
    * 
    * @param  datum
    *         date of the new entry.
    * @param  thema
    *         topic of the new entry.
    * @param  person
    *         person who manage the event.
    * @return id of the new entry.
    */
   public long createEntry(Date datum, String thema, String person) {
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_DATUM, datum.getTime());
      initialValues.put(KEY_THEMA, thema);
      initialValues.put(KEY_PERSON, person);
      return mDb.insert(DATABASE_TABLE, null, initialValues);
   }
   
   /**
    * Deletes the entry with the id from database.
    * 
    * @param  rowId
    *         id, which will be deleted.
    * @return <code>true</code> if entry could be deleted, 
    *         else <code>false</code>.
    */
   public boolean deleteEntry(long rowId) {
      return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
   }
   
   /**
    * Updates an entry with id <code>rowId</code> with the new topic and person.
    * Returns <code>true</code>, if success.
    * 
    * @param  rowId
    *         id to update.
    * @param  thema
    *         new topic.
    * @param  person
    *         new person.
    * @return <code>true</code> if success, else <code>false</code>.
    */
   public boolean updateEntry(long rowId, String thema, String person) {
      ContentValues values = new ContentValues();
      values.put(KEY_THEMA, thema);
      values.put(KEY_PERSON, person);
      return mDb.update
      (DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
   }
   
   /**
    * Returns Cursor for all entries with query or all.
    * 
    * @param  query
    *         Query of the database entries or <code>null</code>.
    * @return Cursor of table "programm".
    */
   public Cursor fetchAllEntries(String query) {
      query = createQuery(query);
      return mDb.query
      (DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DATUM, KEY_THEMA, 
                                     KEY_PERSON}, query, null, null, null, 
                                     KEY_DATUM + " desc");
   }
   
   /**
    * Creates the query from the value.
    * 
    * @param  q
    *         Value is a date, a topic or a person with '-'.
    * @return where-Clause for database query.
    */
   private String createQuery(String q) {
      String query = null;
      
      
      if (q == null) {
         // do nothing -> query is null
      } else if (q.charAt(0) >= '0' && q.charAt(0) <= '9') {
         Date d = getDateFromString(q);
         query = "datum = " + d.getTime();
      } else {
      	String s = q.toUpperCase(def);
      	String tmp = "upper(person) like '%%%s%%' or " +
      			"upper(thema) like '%%%s%%'";
         query = String.format(def, tmp, s, s);
      }
      
      return query;
   }
   
   /**
    * Returns the cursor for entry with the given id.
    * 
    * @param  rowId
    *         id of the selected entry.
    * @return Cursor of the entry or <code>null</code>, if not found.
    * @throws SQLException 
    *         if an error occurs while reading from database.
    */
   public Cursor fetchEntry(long rowId) throws SQLException {
      Cursor mCursor = mDb.query(DATABASE_TABLE, 
                                 new String[] {KEY_ROWID, KEY_DATUM, KEY_THEMA, 
                                               KEY_PERSON}, 
                                               KEY_ROWID + "=" + rowId, null, 
                                               null, null, null);
      if (mCursor != null) {
         mCursor.moveToFirst();
      }
      
      return mCursor;
   }
   
   /**
    * Returns the Date-Object from String.
    * 
    * @param  d
    *         Date as String (Format: dd.MM.yyyy)
    * @return Date-Object.
    */
   public static Date getDateFromString(String d) {
      Date datum = null;
      
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", def);
      try {
         datum = sdf.parse(d);
      } catch (ParseException e) {
         Log.e(TAG, e.getLocalizedMessage());
         datum = null;
      }
      
      return datum;
   }
   
   /**
    * Returns the Date as a String.
    * 
    * @param  t
    *         timestamp in milliseconds (see {@link java.util.Date#getTime()}).
    * @return String of the date.
    */
   public static String getDateString(long t) {
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTimeInMillis(t);
      return DateFormat.format("dd.MM.yyyy", gc).toString();
   }
   
   /**
    * Returns the app data as json.
    * 
    * @return {@link org.json.JSONArray JSONArray}
    */
   public JSONArray getJSonData() {
      JSONArray dataList = new JSONArray();
      JSONObject data;
      
      Cursor c = fetchAllEntries(null);
      
      while (c.moveToNext()) {
         String d = getDateString(c.getLong(1));
         String t = c.getString(2);
         String p = c.getString(3);
         data = new JSONObject();
         try {
            data.put(KEY_DATUM, d);
            data.put(KEY_THEMA, t);
            data.put(KEY_PERSON, p);
            dataList.put(data);
         } catch (JSONException e) {
            Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
         }
      }
      
      return dataList;
   }
   
   /**
    * Writes the JSON data in the database.
    * 
    * @param  data
    *         {@link org.json.JSONArray JSONArray}.
    * @throws JSONException 
    */
   public void writeJSonData(JSONArray data) throws JSONException {
      for (int i = 0;i < data.length();++i) {
         JSONObject o = data.getJSONObject(i);
         Date d = getDateFromString(o.getString(KEY_DATUM));
         String t = o.getString(KEY_THEMA);
         String p = o.getString(KEY_PERSON);
         createEntry(d, t, p);
      }
   }
}
