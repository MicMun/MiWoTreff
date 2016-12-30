package de.micmun.android.miwotreff.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.micmun.android.miwotreff.recyclerview.Program;

/**
 * Provides access to the database.
 *
 * @author MicMun
 * @version 1.0, 29.12.16
 */

public class DBProvider {
   private SQLiteDatabase mDb;
   private int posOfNextWednesday = 0;

   private static DBProvider mDbProvider = null;

   /**
    * Creates a new DBProvider with a context.
    *
    * @param context context for database.
    */
   private DBProvider(Context context) {
      DbHelper dbHelper = new DbHelper(context);
      mDb = dbHelper.getWritableDatabase();
   }

   /**
    * Returns the singleton instance of the DbProvider.
    *
    * @param context context for database.
    * @return DbProvider instance.
    */
   public static DBProvider getInstance(Context context) {
      if (mDbProvider == null)
         mDbProvider = new DBProvider(context);

      return mDbProvider;
   }

   /**
    * Close the database connection.
    */
   public void close() {
      mDb.close();
      mDbProvider = null;
   }

   /**
    * Returns a list of programs.
    *
    * @param selection     where clause or null, if no selection.
    * @param selectionArgs arguments for selection or null.
    * @return list of programs.
    */
   public List<Program> queryProgram(String selection, String[] selectionArgs, String sort) {
      List<Program> programs = new ArrayList<>();

      String[] projection = {DBConstants._ID, DBConstants.KEY_DATUM, DBConstants.KEY_THEMA,
            DBConstants.KEY_PERSON, DBConstants.KEY_EDIT};

      if (sort == null) {
         sort = DBConstants.KEY_DATUM + " desc";
      }

      Cursor c = mDb.query(DBConstants.TABLE_NAME, projection, selection, selectionArgs, null, null,
            sort);

      if (c != null) {
         while (c.moveToNext()) {
            long _id = c.getLong(c.getColumnIndex(DBConstants._ID));
            long date = c.getLong(c.getColumnIndex(DBConstants.KEY_DATUM));
            String topic = c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
            String person = c.getString(c.getColumnIndex(DBConstants.KEY_PERSON));
            boolean edit = c.getInt(c.getColumnIndex(DBConstants.KEY_EDIT)) == 1;
            Program p = new Program(_id, date, topic, person, edit);
            if (p.isNextWednesDay())
               posOfNextWednesday = programs.size();
            programs.add(p);
         }
         c.close();
      }

      return programs;
   }

   /**
    * Returns the position of the next wednesday program or <code>0</code>, if no queryProgram was
    * called.
    *
    * @return the position of the next wednesday program or <code>0</code>, if no queryProgram was
    * called.
    */
   public int getPosOfNextWednesday() {
      return posOfNextWednesday;
   }

   /**
    * Returns the existend program or <code>null</code>, if it does not exist.
    *
    * @param program program with date to check.
    * @return the existend program or <code>null</code>, if it does not exist.
    */
   public Program programExists(Program program) {
      String selection = "strftime('%d.%m.%Y', (" + DBConstants.KEY_DATUM +
            "/1000), 'unixepoch', 'localtime') = ?";
      String[] selectionArgs = {program.getDateString()};

      List<Program> programs = queryProgram(selection, selectionArgs, null);
      Program p = null;

      if (programs.size() == 1) {
         p = programs.get(0);
      }

      return p;
   }

   /**
    * Returns the last date of programs.
    *
    * @return last date of programs.
    */
   public String getLastDate() {
      String selection = DBConstants.KEY_DATUM +
            " = (SELECT max(" + DBConstants.KEY_DATUM + ") FROM " + DBConstants.TABLE_NAME +
            ")";
      List<Program> programs = queryProgram(selection, null, null);
      String lastDate;

      if (programs.size() == 1) {
         lastDate = programs.get(0).getDateString();
      } else {
         lastDate = DBDateUtility.getDateString(Calendar.getInstance().getTimeInMillis());
      }

      return lastDate;
   }

   /**
    * Returns a map with the settings (key -> value).
    *
    * @param selection     where clause or <code>null</code>, if no selection wanted.
    * @param selectionArgs selection arguments.
    * @return map with settings.
    */
   private Map<String, String> getSettings(String selection, String[] selectionArgs) {
      Map<String, String> settings = new HashMap<>();

      String[] projection = {DBConstants.KEY_KEY, DBConstants.KEY_VALUE};
      Cursor c = mDb.query(DBConstants.SETTING_TABLE_NAME, projection, selection, selectionArgs,
            null, null, null);

      if (c != null) {
         while (c.moveToNext()) {
            String key = c.getString(c.getColumnIndex(DBConstants.KEY_KEY));
            String value = c.getString(c.getColumnIndex(DBConstants.KEY_VALUE));
            settings.put(key, value);
         }
         c.close();
      }

      return settings;
   }

   /**
    * Returns the setting value for last update.
    *
    * @return the setting value for last update.
    */
   public String getLastUpdate() {
      String selection = DBConstants.KEY_KEY + " = ?";
      String[] selectionArgs = {DBConstants.SETTING_KEY_LAST_UPDATE};

      Map<String, String> settings = getSettings(selection, selectionArgs);
      String lastUpdate = DBConstants.SETTING_VALUE_LAST_UPDATE;

      if (settings.size() > 0) {
         lastUpdate = settings.get(DBConstants.SETTING_KEY_LAST_UPDATE);
      }

      return lastUpdate;
   }

   /**
    * Inserts a program into database.
    *
    * @param p Program to insert.
    */
   public void insertProgram(Program p) {
      ContentValues values = new ContentValues();
      values.put(DBConstants.KEY_DATUM, p.getDate());
      values.put(DBConstants.KEY_THEMA, p.getTopic());
      values.put(DBConstants.KEY_PERSON, p.getPerson());

      long id = mDb.insertWithOnConflict(DBConstants.TABLE_NAME, null, values,
            SQLiteDatabase.CONFLICT_REPLACE);
      if (id != -1) {
         p.set_id(id);
      }
   }

   /**
    * Updates a program and returns the number <code>1</code>, if succeed.
    *
    * @param p program to update.
    * @return number of updated entries.
    */
   public int updateProgram(Program p) {
      ContentValues values = new ContentValues();
      values.put(DBConstants._ID, p.get_id());
      values.put(DBConstants.KEY_DATUM, p.getDate());
      values.put(DBConstants.KEY_THEMA, p.getTopic());
      values.put(DBConstants.KEY_PERSON, p.getPerson());
      values.put(DBConstants.KEY_EDIT, p.isEdited() ? 1 : 0);

      String selection = DBConstants._ID + " = ?";
      String[] selectionArgs = {String.valueOf(p.get_id())};
      return mDb.update(DBConstants.TABLE_NAME, values, selection, selectionArgs);
   }

   /**
    * Updates a setting and returns the number <code>1</code>, if succeed.
    *
    * @param key   key of the setting.
    * @param value new value of the setting.
    * @return number of updated entries.
    */
   public int updateSetting(String key, String value) {
      ContentValues values = new ContentValues();
      values.put(DBConstants.KEY_KEY, key);
      values.put(DBConstants.KEY_VALUE, value);

      String selection = DBConstants.KEY_KEY + " = ?";
      String[] selectionArgs = {key};

      return mDb.update(DBConstants.SETTING_TABLE_NAME, values, selection, selectionArgs);
   }
}
