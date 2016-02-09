/*
 * Copyright 2016 MicMun
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
package de.micmun.android.miwotreff.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.micmun.android.miwotreff.db.DBConstants;

/**
 * Manages the preferences of the app.
 *
 * @author MicMun
 * @version 1.0, 09.02.16
 */
public class AppPreferences {
   private SharedPreferences preferences;

   /**
    * Creates a new AppPreferences object.
    *
    * @param ctx context to get shared preferences.
    */
   public AppPreferences(Context ctx) {
      preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
   }

   /**
    * Returns <code>true</code>, if the auto sync is on, else <code>false</code>.
    *
    * @return <code>true</code>, if the auto sync is on, else <code>false</code>.
    */
   public boolean isAutoSync() {
      return preferences.getBoolean(DBConstants.SETTING_KEY_AUTO_SYNC, true);
   }

   /**
    * Returns the number of files to keep while deleting old backup files.
    *
    * @return number of files to keep while deleting old backup files.
    */
   public int getNumberOfFilesToKeep() {
      String numberOfFiles = preferences.getString(DBConstants.SETTING_KEY_NUMBER_OLD,
            String.valueOf(DBConstants.SETTING_VALUE_NUMBER_OLD));
      return Integer.parseInt(numberOfFiles);
   }
}
