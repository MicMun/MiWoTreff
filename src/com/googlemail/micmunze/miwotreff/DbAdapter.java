/**
 * DbAdapter.java
 *
 * Copyright 2012 by Michael Munzert
 */
package com.googlemail.micmunze.miwotreff;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Klasse f&uuml;r den Datenbankzugriff.
 * Erstellt die Datenbank und stellt die &uuml;blichen CRUD-Operationen
 * bereit.
 *
 * @author Michael Munzert
 * @version 1.0, 05.07.2012
 */
public class DbAdapter
{
   public static final String KEY_ROWID = "_id"; // Primary Key _id
   public static final String KEY_DATUM = "datum"; // Spalte Datum
   public static final String KEY_THEMA = "thema"; // Spalte Thema
   public static final String KEY_PERSON = "person"; // Spalte Person
   
   //private static final String TAG = "DbAdapter";
   
   private static final String DATABASE_NAME = "miwotreff";
   private static final String DATABASE_TABLE = "programm";
   private static final int DATABASE_VERSION = 1;
   
   /* Create Statement f&uuml;r Tabelle "programm" */
   private static final String TABLE_CREATE = 
   "CREATE TABLE programm (_id integer primary key autoincrement," +
   " datum integer not null unique, thema text not null, person text);";
   
   private SQLiteDatabase mDb; // Database SQLITE
   private DatabaseHelper mDbHelper;
   private final Context mCtx; // Context fuer Database
   
   /**
    * Datenbank-Zugriff erstellt die Tabelle und die Datenbank.
    * Implementiert die Erstellung des Schemas.
    *
    * @author Michael Munzert
    * @version 1.0, 05.07.2012
    */
   private static class DatabaseHelper extends SQLiteOpenHelper {
      /**
       * Neuer DatabaseHelper mit einem Context.
       * 
       * @param  context
       *         Context der Datenbank.
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
    * Erstellt einen neuen DbAdapter mit einem Context f&uuml;r die Datenbank.
    * 
    * @param  ctx
    *         Context f&uuml;r die Datenbank.
    */
   public DbAdapter (Context ctx) {
      this.mCtx = ctx;
   }
   
   /**
    * &Ouml;ffnet die Datenbankverbindung und liefert eine Referenz auf sich
    * selbst.
    * 
    * @return Referenz auf sich selbst.
    * @throws SQLException
    *         wenn Datenbankverbindung nicht hergestellt werden konnte.
    */
   public DbAdapter open () throws SQLException {
      mDbHelper = new DatabaseHelper (mCtx);
      mDb = mDbHelper.getWritableDatabase ();
      return this;
   }
   
   /**
    * Schlie&szlig;t die Datenbankverbindung.
    */
   public void close () {
      mDbHelper.close ();
   }
   
   /**
    * F&uuml;gt einen Eintrag bestehend aus Datum, Thema und Person in die
    * Datenbank ein und liefert die ID des neuen Eintrags.
    * 
    * @param  datum
    *         Datum des Programmpunktes.
    * @param  thema
    *         Thema an diesem Mittwoch.
    * @param  person
    *         gestaltende Person.
    * @return ID des neuen Eintrags.
    */
   public long createEntry (Date datum, String thema, String person) {
      ContentValues initialValues = new ContentValues ();
      initialValues.put (KEY_DATUM, datum.getTime ());
      initialValues.put (KEY_THEMA, thema);
      initialValues.put (KEY_PERSON, person);
      return mDb.insert (DATABASE_TABLE, null, initialValues);
   }
   
   /**
    * L&ouml;scht einen Eintrag mit der ID aus der Datenbank.
    * 
    * @param  rowId
    *         zu l&ouml;schende ID.
    * @return <code>true</code> wenn der Eintrag erfolgreich gel&ouml;scht
    *         sonst <code>false</code>.
    */
   public boolean deleteEntry (long rowId) {
      return mDb.delete (DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
   }
   
   /**
    * Aktualisiert den Eintrag mit ID <code>rowId</code> mit den neuen Thema und
    * Person und liefert <code>true</code> bei Erfolg.
    * 
    * @param  rowId
    *         zu aktualisierende ID.
    * @param  thema
    *         neues Thema.
    * @param  person
    *         neue Person.
    * @return <code>true</code> bei Erfolg, sonst <code>false</code>.
    */
   public boolean updateEntry (long rowId, String thema, String person) {
      ContentValues values = new ContentValues ();
      values.put (KEY_THEMA, thema);
      values.put (KEY_PERSON, person);
      return mDb.update (DATABASE_TABLE, values, 
                         KEY_ROWID + "=" + rowId, null) > 0;
   }
   
   /**
    * Liefert Cursor &uuml;ber alle Eintr&auml;ge.
    * 
    * @return Cursor der Tabelle "programm".
    */
   public Cursor fetchAllEntries () {
      return mDb.query (DATABASE_TABLE, 
                        new String [] {KEY_ROWID, KEY_DATUM, KEY_THEMA, 
                                       KEY_PERSON}, 
                        null, null, null, null, KEY_DATUM + " desc");
   }
   
   /**
    * Liefert Cursor f&uuml;r einen Eintrag mit der gegebenen ID.
    * 
    * @param  rowId
    *         ID des zu lesenden Falls.
    * @return Cursor des Eintrags oder <code>null</code>, wenn nicht gefunden.
    * @throws SQLException 
    *         wenn ein Fehler beim Lesen aus der Datenbank auftritt.
    */
   public Cursor fetchEntry (long rowId) throws SQLException {
      Cursor mCursor = mDb.query (DATABASE_TABLE, 
                                  new String [] {KEY_ROWID, KEY_DATUM, KEY_THEMA, 
                                                 KEY_PERSON}, 
                                  KEY_ROWID + "=" + rowId, null, null, null, 
                                  null);
      if (mCursor != null) {
         mCursor.moveToFirst ();
      }
      
      return mCursor;
   }
}
