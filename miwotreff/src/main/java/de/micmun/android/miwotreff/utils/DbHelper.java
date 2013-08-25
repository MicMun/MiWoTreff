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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles the database. Creates the database with the table and handles the
 * CRUD-Methods (Create, Update, Delete) and the selections.
 *
 * @author MicMun
 * @version 2.0, 18.01.2013
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    private static final String DATABASE_NAME = "miwotreff";
    private static final int DATABASE_VERSION = 2;
    /* Create Statement for table "programm" */
    private static final String TABLE_CREATE = "CREATE TABLE " + DBConstants.TABLE_NAME +
            " (" + DBConstants._ID + " integer primary key autoincrement, " +
            DBConstants.KEY_DATUM + " integer not null unique, " +
            DBConstants.KEY_THEMA + " text not null, " +
            DBConstants.KEY_PERSON + " text, " +
            DBConstants.KEY_EDIT + " integer default 0 not null);";

    /**
     * Creates a new DbHelper with a context for the database.
     *
     * @param ctx Context for the database.
     */
    public DbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // from version 1 to 2
        switch (newVersion) {
            case 2:
                String sql = "ALTER TABLE " + DBConstants.TABLE_NAME +
                        " ADD COLUMN " + DBConstants.KEY_EDIT + " integer default 0 not null;";
                db.execSQL(sql);
                break;
        }
    }
}
