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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles the database. Creates the database with the table and handles the
 * CRUD-Methods (Create, Update, Delete) and the selections.
 *
 * @author Michael Munzert
 * @version 2.1, 29.12.16
 */
class DbHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "miwotreff";
   private static final int DATABASE_VERSION = 2;
   /* Create statement for table "programm" */
   private final String PROGRAM_CREATE = "CREATE TABLE " + DBConstants.TABLE_NAME +
         " (" + DBConstants._ID + " integer primary key autoincrement, " +
         DBConstants.KEY_DATUM + " integer not null unique, " +
         DBConstants.KEY_THEMA + " text not null, " +
         DBConstants.KEY_PERSON + " text, " +
         DBConstants.KEY_EDIT + " integer default 0 not null);";
   /* Create statement for table "settings" */
   private final String SETTINGS_CREATE = "CREATE TABLE " + DBConstants.SETTING_TABLE_NAME +
         " (" + DBConstants.KEY_KEY + " text primary key, " + DBConstants.KEY_VALUE + " text);";

   /**
    * Creates a new DbHelper with a context for the database.
    *
    * @param ctx Context for the database.
    */
   DbHelper(Context ctx) {
      super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(PROGRAM_CREATE);
      db.execSQL(SETTINGS_CREATE);

      // Default values
      ContentValues lastUpdate = new ContentValues(2);
      lastUpdate.put(DBConstants.KEY_KEY, DBConstants.SETTING_KEY_LAST_UPDATE);
      lastUpdate.put(DBConstants.KEY_VALUE, DBConstants.SETTING_VALUE_LAST_UPDATE);
      // insert last update
      db.insert(DBConstants.SETTING_TABLE_NAME, null, lastUpdate);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion == 1 && newVersion == 2) {
         db.execSQL(SETTINGS_CREATE);
         // Default values
         ContentValues lastUpdate = new ContentValues(2);
         lastUpdate.put(DBConstants.KEY_KEY, DBConstants.SETTING_KEY_LAST_UPDATE);
         lastUpdate.put(DBConstants.KEY_VALUE, DBConstants.SETTING_VALUE_LAST_UPDATE);
         // insert lastUpdate
         db.insert(DBConstants.SETTING_TABLE_NAME, null, lastUpdate);
      }
   }
}
