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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Constants for the database.
 * Provides Keys for columns, content uris, etc.
 *
 * @author Michael Munzert
 * @version 1.0, 12.08.13.
 */
public class DBConstants implements BaseColumns {
   /**
    * Key for column <code>p_date</code> (date).
    */
   public static final String KEY_DATUM = "p_date";
   /**
    * Key for column <code>p_topic</code> (topic).
    */
   public static final String KEY_THEMA = "p_topic";
   /**
    * Key for column <code>p_person</code> (person).
    */
   public static final String KEY_PERSON = "p_person";
   /**
    * Key for column <code>edit</code> (edit).
    */
   public static final String KEY_EDIT = "edit";
   /**
    * Name of the table.
    */
   public static final String TABLE_NAME = "programm";
   /**
    * Authority for the content provider.
    */
   public static final String AUTHORITY = "de.micmun.android.miwotreff.provider";
   /**
    * Base of content uris.
    */
   public static final String BASE_URI = "content://" + AUTHORITY;
   /**
    * Content uri.
    */
   public static final Uri TABLE_CONTENT_URI = Uri.parse(BASE_URI + "/" + TABLE_NAME);
   /**
    * Name for query with date.
    */
   public static final String DATE_QUERY = "withdate";
   /**
    * Name for sync query with date.
    */
   public static final String SYNC_QUERY = "syncdate";
   /**
    * Name for last date query.
    */
   public static final String LAST_DATE_QUERY = "lastdate";
   /**
    * Table for settings and last update date.
    */
   public static final String SETTING_TABLE_NAME = "settings";
   /**
    * Column with the key of the setting.
    */
   public static final String KEY_KEY = "key";
   /**
    * Column with the value of the setting.
    */
   public static final String KEY_VALUE = "value";
   /**
    * Key for last update setting.
    */
   public static final String SETTING_KEY_LAST_UPDATE = "last_update";
   /**
    * Key for boolean automatic setting.
    */
   public static final String SETTING_KEY_AUTO_SYNC = "auto_sync";
   /**
    * Key for number of old files.
    */
   public static final String SETTING_KEY_NUMBER_OLD = "number_old_files";
   /**
    * Default value of the number_old_files.
    */
   public static final String SETTING_VALUE_NUMBER_OLD = "5";
   /**
    * Default value for last update.
    */
   public static final String SETTING_VALUE_LAST_UPDATE = "28.10.2015";
   /**
    * Name for key query.
    */
   public static final String KEY_QUERY = "keyquery";
   /**
    * Uri for setting table.
    */
   public static final Uri SETTING_CONTENT_URI = Uri.parse(BASE_URI + "/" + SETTING_TABLE_NAME);
}
