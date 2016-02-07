/**
 * Copyright 2015-2016 MicMun
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
package de.micmun.android.miwotreff.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.ArrayList;

/**
 * Helper for syncing calendar.
 *
 * @author MicMun
 * @version 1.0, 25.02.2015
 */
public class CalendarSyncHelper {
   private static final String[] EVENT_PROJECTION = new String[]{
         CalendarContract.Calendars._ID,                           // 0
         CalendarContract.Calendars.CALENDAR_DISPLAY_NAME          // 1
   };

   // The indices for the projection array above.
   private static final int PROJECTION_ID_INDEX = 0;
   private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;

   /**
    * Returns the users calendars.
    *
    * @param context context of application.
    * @return calendars.
    */
   public static ArrayList<CalendarInfo> getCalendars(Context context) {
      ArrayList<CalendarInfo> calList = new ArrayList<>();
      String selection = CalendarContract.Calendars.VISIBLE + " = 1 AND "
            + CalendarContract.Calendars.ACCOUNT_NAME + " = "
            + CalendarContract.Calendars.OWNER_ACCOUNT;
      String orderBy = CalendarContract.Calendars._ID;
      //noinspection ResourceType
      Cursor c = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
            EVENT_PROJECTION, selection, null, orderBy);

      if (c != null) {
         while (c.moveToNext()) {
            // Get the field values
            long calID = c.getLong(PROJECTION_ID_INDEX);
            String displayName = c.getString(PROJECTION_DISPLAY_NAME_INDEX);

            CalendarInfo cinfo = new CalendarInfo(calID, displayName);
            calList.add(cinfo);
         }
         c.close();
      }

      return calList;
   }

   /**
    * Syncs the program with the calendar.
    *
    * @param calendarInfo calendar info with the calendar to sync.
    */
   public static void syncCalendar(Context context, CalendarInfo calendarInfo,
                                   CalendarSyncTask.OnEventRefreshListener listener) {
      // sync program with the choosen calendar
      CalendarSyncTask cst = new CalendarSyncTask(context, calendarInfo);
      cst.setOnEventRefreshedListener(listener);
      cst.execute();
   }
}
