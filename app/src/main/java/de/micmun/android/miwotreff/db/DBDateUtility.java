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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Beschreibung.
 *
 * @author Michael Munzert
 * @version 1.1, 29.12.16
 */
public class DBDateUtility {
   private static final String TAG = "DBDateUtility";

   private static final Locale def = Locale.getDefault();
   private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Berlin");

   /**
    * Returns the Date-Object from String.
    *
    * @param d Date as String (Format: dd.MM.yyyy)
    * @return Date-Object.
    */
   public static Date getDateFromString(String d) {
      Date datum;

      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", def);
      sdf.setTimeZone(TIMEZONE);

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
      gc.setTimeZone(TIMEZONE);

      return DateFormat.format("dd.MM.yyyy", gc).toString();
   }

   /**
    * Returns the date of next wednesday.
    *
    * @return date of next wednesday as calendar.
    */
   public static String getNextWednesday() {
      Calendar nextWedDay = Calendar.getInstance();
      nextWedDay.setTimeInMillis(System.currentTimeMillis());
      nextWedDay.setTimeZone(TIMEZONE);

      int diff = GregorianCalendar.WEDNESDAY -
            nextWedDay.get(GregorianCalendar.DAY_OF_WEEK);

      if (!(diff >= 0)) {
         diff += 7;
      }

      nextWedDay.add(Calendar.DAY_OF_MONTH, diff);
      nextWedDay.set(Calendar.HOUR_OF_DAY, 0);
      nextWedDay.set(Calendar.MINUTE, 0);
      nextWedDay.set(Calendar.SECOND, 0);
      nextWedDay.set(Calendar.MILLISECOND, 0);

      return getDateString(nextWedDay.getTimeInMillis());
   }
}
