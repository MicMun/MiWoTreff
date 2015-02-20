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

import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Beschreibung.
 *
 * @author Michael Munzert
 * @version 1.0, 12.08.13.
 */
public class DBDateUtility {
    private static final String TAG = "DBDateUtility";
    private static final Locale def = Locale.getDefault();

    /**
     * Returns the Date-Object from String.
     *
     * @param d Date as String (Format: dd.MM.yyyy)
     * @return Date-Object.
     */
    public static Date getDateFromString(String d) {
        Date datum;

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
     * @param t timestamp in milliseconds (see {@link java.util.Date#getTime()}).
     * @return String of the date.
     */
    public static String getDateString(long t) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(t);
        return DateFormat.format("dd.MM.yyyy", gc).toString();
    }
}
