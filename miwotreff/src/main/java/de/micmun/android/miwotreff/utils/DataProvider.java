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

package de.micmun.android.miwotreff.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Content provider for the miwotreff database.
 *
 * @author Michael Munzert
 * @version 1.0, 12.08.2013
 */
public class DataProvider extends ContentProvider {
   // IDs for matching content
   private static final int ROOT_ID = 0;
   private static final int TABLE_PROGRAM_ID = 10;
   private static final int PROGRAM_POINT_ID = 11;
   private static final int PROGRAM_DATE_ID = 12;
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
                  + DBConstants.DATE_QUERY,
            PROGRAM_DATE_ID
      );
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
   public Cursor query(Uri uri, String[] projection, String selection,
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
            selection = DBConstants.KEY_DATUM + " = ?";
            res = mDb.query(DBConstants.TABLE_NAME, projection, selection,
                  selectionArgs, null, null, null);
            break;
      }
      return res;
   }

   /**
    * @see android.content.ContentProvider#getType(android.net.Uri)
    */
   @Override
   public String getType(Uri uri) {
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
      }

      return type;
   }

   /**
    * @see android.content.ContentProvider#insert(android.net.Uri,
    * android.content.ContentValues)
    */
   @Override
   public Uri insert(Uri uri, ContentValues values) {
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
   public int update(Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
      int count = 0;

      switch (mUriMatcher.match(uri)) {
         case PROGRAM_POINT_ID:
            selection = DBConstants._ID + " = ?";
            selectionArgs = new String[]{uri.getLastPathSegment()};
            count = mDb.update(DBConstants.TABLE_NAME, values, selection,
                  selectionArgs);
            getContext().getContentResolver().notifyChange(DBConstants
                  .TABLE_CONTENT_URI, null);
            break;
      }
      return count;
   }
}
