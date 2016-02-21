/*
 * Copyright 2013-2014 MicMun
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU >General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or >(at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * >without even the implied warranty of MERCHANTABILIT or FITNESS FOR A PARTICULAR PURPOSE.
 * >See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see >http://www.gnu.org/licenses/.
 */

package de.micmun.android.miwotreff.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Content provider for the miwotreff database.
 *
 * @author Michael Munzert
 * @version 1.0, 12.08.2013
 */
public class DataProvider extends ContentProvider {
   // IDs for matching content
   private static final int ROOT_ID = 0;
   private static final int TABLE_PROGRAM_ID = 10; // complett entries of program
   private static final int PROGRAM_POINT_ID = 11; // single entry with id
   private static final int PROGRAM_DATE_ID = 12; // single entry with date (only id and date)
   private static final int PROGAM_SYNC_DATE = 13; // all entries with date >= parameter
   private static final int PROGRAM_LAST_DATE = 14; // last date entry
   private static final int TABLE_SETTING_ID = 20; // all settings
   private static final int SETTING_KEY_ID = 21; // setting with given key
   private static final UriMatcher mUriMatcher = new UriMatcher(ROOT_ID);

   /**
    * Static constructor for the uri's to match.
    */
   static {
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME,
            TABLE_PROGRAM_ID);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME + "/#",
            PROGRAM_POINT_ID);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME + "/"
            + DBConstants.DATE_QUERY, PROGRAM_DATE_ID);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME + "/"
            + DBConstants.SYNC_QUERY, PROGAM_SYNC_DATE);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.TABLE_NAME + "/"
            + DBConstants.LAST_DATE_QUERY, PROGRAM_LAST_DATE);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.SETTING_TABLE_NAME, TABLE_SETTING_ID);
      mUriMatcher.addURI(DBConstants.AUTHORITY, DBConstants.SETTING_TABLE_NAME + "/"
            + DBConstants.KEY_QUERY, SETTING_KEY_ID);
   }

   private SQLiteDatabase mDb;

   /**
    * @see android.content.ContentProvider#onCreate()
    */
   @Override
   public boolean onCreate() {
      DbHelper mDbHelper = new DbHelper(getContext());
      mDb = mDbHelper.getWritableDatabase();
      return true;
   }

   /**
    * @see ContentProvider#query(android.net.Uri, String[], String, String[],
    * String)
    */
   @Override
   public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
      Cursor res = null;
      switch (mUriMatcher.match(uri)) {
         case TABLE_PROGRAM_ID:
            projection = new String[]{DBConstants._ID, DBConstants.KEY_DATUM,
                  DBConstants.KEY_THEMA, DBConstants.KEY_PERSON,
                  DBConstants.KEY_EDIT};
            sortOrder = DBConstants.KEY_DATUM + " desc";
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection,
                  selectionArgs, null, null, sortOrder);
            res.setNotificationUri(getContext().getContentResolver(), uri);
            break;
         case PROGRAM_POINT_ID:
            projection = new String[]{DBConstants._ID, DBConstants.KEY_DATUM,
                  DBConstants.KEY_THEMA, DBConstants.KEY_PERSON,
                  DBConstants.KEY_EDIT};
            selection = DBConstants._ID + " = ?";
            selectionArgs = new String[]{uri.getLastPathSegment()};
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection,
                  selectionArgs, null, null, null);
            break;
         case PROGRAM_DATE_ID:
            projection = new String[]{DBConstants._ID, DBConstants.KEY_EDIT};
            selection = "strftime('%d.%m.%Y', (" + DBConstants.KEY_DATUM +
                  "/1000), 'unixepoch', 'localtime') = ?";
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection, selectionArgs,
                  null, null, null);
            break;
         case PROGAM_SYNC_DATE:
            projection = new String[]{DBConstants.KEY_DATUM, DBConstants.KEY_THEMA,
                  DBConstants.KEY_PERSON};
            selection = DBConstants.KEY_DATUM + " >= ?";
            sortOrder = DBConstants.KEY_DATUM;
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection, selectionArgs,
                  null, null, sortOrder);
            break;
         case PROGRAM_LAST_DATE:
            projection = new String[]{DBConstants._ID, DBConstants.KEY_DATUM};
            selection = DBConstants.KEY_DATUM +
                  " = (SELECT max(" + DBConstants.KEY_DATUM + ") FROM " + DBConstants.TABLE_NAME +
                  ")";
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection, null, null, null,
                  sortOrder);
            break;
         case TABLE_SETTING_ID:
            projection = new String[]{DBConstants.KEY_KEY, DBConstants.KEY_VALUE};
            res = mDb.query(DBConstants.SETTING_TABLE_NAME, projection, null, null, null, null,
                  null);
            break;
         case SETTING_KEY_ID:
            projection = new String[]{DBConstants.KEY_VALUE};
            selection = DBConstants.KEY_KEY + " = ?";
            res = mDb.query(DBConstants.SETTING_TABLE_NAME, projection, selection, selectionArgs,
                  null, null, null);
            break;
      }
      return res;
   }

   /**
    * @see android.content.ContentProvider#getType(android.net.Uri)
    */
   @Override
   public String getType(@NonNull Uri uri) {
      String type = null;

      switch (mUriMatcher.match(uri)) {
         case TABLE_PROGRAM_ID:
            type = "vnd.android.cursor.dir/vnd." + DBConstants.AUTHORITY + "."
                  + DBConstants.TABLE_NAME;
            break;
         case PROGRAM_POINT_ID:
            type = "vnd.android.cursor.item/vnd." + DBConstants.AUTHORITY + "" +
                  "." + DBConstants.TABLE_NAME;
            break;
         case PROGRAM_DATE_ID:
            type = "vnd.android.cursor.item/vnd." + DBConstants.AUTHORITY + "" +
                  "." + DBConstants.TABLE_NAME + "." + DBConstants.DATE_QUERY;
            break;
         case PROGAM_SYNC_DATE:
            type = "vnd.android.cursor.dir/vnd." + DBConstants.AUTHORITY + "."
                  + DBConstants.TABLE_NAME;
            break;
         case PROGRAM_LAST_DATE:
            type = "vnd.android.cursor.item/vnd." + DBConstants.AUTHORITY + "" +
                  "." + DBConstants.TABLE_NAME + "." + DBConstants.LAST_DATE_QUERY;
            break;
         case TABLE_SETTING_ID:
            type = "vnd.android.cursor.dir/vnd." + DBConstants.AUTHORITY + "."
                  + DBConstants.SETTING_TABLE_NAME;
            break;
         case SETTING_KEY_ID:
            type = "vnd.android.cursor.item/vnd." + DBConstants.AUTHORITY + "" +
                  "." + DBConstants.SETTING_TABLE_NAME + "." + DBConstants.KEY_QUERY;
            break;
      }

      return type;
   }

   /**
    * @see android.content.ContentProvider#insert(android.net.Uri,
    * android.content.ContentValues)
    */
   @Override
   public Uri insert(@NonNull Uri uri, ContentValues values) {
      Uri res = null;

      switch (mUriMatcher.match(uri)) {
         case TABLE_PROGRAM_ID:
            long id = mDb.insertWithOnConflict(DBConstants.TABLE_NAME, null,
                  values, SQLiteDatabase.CONFLICT_REPLACE);
            if (id != -1) {
               res = Uri.withAppendedPath(uri, String.valueOf(id));
               getContext().getContentResolver().notifyChange(uri, null);
            }
      }
      return res;
   }

   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      switch (mUriMatcher.match(uri)) {
         case PROGRAM_POINT_ID:
            selection = DBConstants._ID + " = ?";
            selectionArgs = new String[]{uri.getLastPathSegment()};
            getContext().getContentResolver().notifyChange(DBConstants
                  .TABLE_CONTENT_URI, null);
            return mDb.delete(DBConstants.TABLE_NAME, selection,
                  selectionArgs);
      }
      return 0;
   }

   /**
    * @see android.content.ContentProvider#update(android.net.Uri,
    * android.content.ContentValues, String, String[])
    */
   @Override
   public int update(@NonNull Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
      int count = 0;

      switch (mUriMatcher.match(uri)) {
         case PROGRAM_POINT_ID:
            selection = DBConstants._ID + " = ?";
            selectionArgs = new String[]{uri.getLastPathSegment()};
            count = mDb.update(DBConstants.TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(DBConstants.TABLE_CONTENT_URI, null);
            break;
         case SETTING_KEY_ID:
            selection = DBConstants.KEY_KEY + " = ?";
            count = mDb.update(DBConstants.SETTING_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(DBConstants.SETTING_CONTENT_URI, null);
            break;
      }
      return count;
   }
}
