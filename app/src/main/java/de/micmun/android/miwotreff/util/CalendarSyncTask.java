/*
 * Copyright 2015 MicMun
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.db.DBProvider;
import de.micmun.android.miwotreff.recyclerview.Program;

/**
 * Background to synchronise events with calendar.
 *
 * @author MicMun
 * @version 1.1, 29.12.16
 */
public class CalendarSyncTask extends AsyncTask<Void, Void, Integer> {
   private final String EVENT_LOC;
   private final String EVENT_DESC;
   private final Context mCtx;
   private final CalendarInfo mCalInfo;
   private final long TODAY_MILLIS;
   private OnEventRefreshListener mOnEventRefreshListener;

   CalendarSyncTask(Context ctx, CalendarInfo calendarInfo) {
      mCtx = ctx;
      mCalInfo = calendarInfo;

      // Constant values
      EVENT_LOC = mCtx.getString(R.string.cal_loc);
      EVENT_DESC = mCtx.getString(R.string.app_name);
      TODAY_MILLIS = Calendar.getInstance().getTime().getTime();
   }

   @Override
   protected Integer doInBackground(Void... params) {
      List<Program> programList = getProgramToSync();
      if (programList.size() <= 0) { // no data to sync
         return 0;
      }
      HashMap<String, Long> eventMap = getEvents();
      int counter = 0;

      for (Program p : programList) {
         long date = p.getDate();
         String topic = p.getTopic();
         String person = p.getPerson();

         // start and end
         long startMillis = 0;
         long endMillis = 0;
         Calendar beginTime = Calendar.getInstance();
         beginTime.setTimeInMillis(date);
         beginTime.set(Calendar.HOUR_OF_DAY, 19);
         beginTime.set(Calendar.MINUTE, 30);
         beginTime.set(Calendar.SECOND, 0);
         startMillis = beginTime.getTimeInMillis();
         Calendar endTime = Calendar.getInstance();
         endTime.setTimeInMillis(date);
         endTime.set(Calendar.HOUR_OF_DAY, 21);
         endTime.set(Calendar.MINUTE, 0);
         endTime.set(Calendar.SECOND, 0);
         endMillis = endTime.getTimeInMillis();

         String title = mCtx.getString(R.string.cal_prefix) + topic + " (" + person + ")";

         // Values for insert or update in calendar
         ContentValues cv = new ContentValues();
         cv.put(CalendarContract.Events.DTSTART, startMillis);
         cv.put(CalendarContract.Events.DTEND, endMillis);
         cv.put(CalendarContract.Events.TITLE, title);
         cv.put(CalendarContract.Events.DESCRIPTION, EVENT_DESC);
         cv.put(CalendarContract.Events.CALENDAR_ID, mCalInfo.getId());
         cv.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
         cv.put(CalendarContract.Events.EVENT_LOCATION, EVENT_LOC);

         String key = DBDateUtility.getDateString(date);
         long id = eventMap.containsKey(key) ? eventMap.get(key) : -1;

         ContentResolver cr = mCtx.getContentResolver();

         if (id == -1) { // insert event
            cr.insert(CalendarContract.Events.CONTENT_URI, cv);
         } else { // update event
            Uri uri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, String.valueOf(id));
            cr.update(uri, cv, null, null);
         }
         counter++;
      }

      return counter;
   }

   /**
    * Returns a cursor with program entries.
    *
    * @return a cursor with program entries.
    */
   private List<Program> getProgramToSync() {
      String selection = DBConstants.KEY_DATUM + " >= ?";
      String sortOrder = DBConstants.KEY_DATUM;
      String[] selArgs = new String[]{String.valueOf(TODAY_MILLIS)};

      return DBProvider.getInstance(mCtx).queryProgram(selection, selArgs, sortOrder);
   }

   /**
    * Returns cursor with the events from calendar.
    *
    * @return cursor with the events from calendar.
    */
   private HashMap<String, Long> getEvents() {
      ContentResolver cr = mCtx.getContentResolver();
      Uri uEvents = CalendarContract.Events.CONTENT_URI;
      String[] projection = new String[]{CalendarContract.Events._ID, "strftime('%d.%m.%Y', "
            + CalendarContract.Events.DTSTART + "/1000, 'unixepoch') AS DATUM"};
      String selection = CalendarContract.Events.DTSTART + " >= ? AND "
            + CalendarContract.Events.DESCRIPTION + " = ? AND "
            + CalendarContract.Events.EVENT_LOCATION + " = ?";
      // Start time from now
      String dtstart = Long.toString(Calendar.getInstance().getTimeInMillis());
      String[] selArgs = new String[]{dtstart, EVENT_DESC, EVENT_LOC};
      String orderBy = CalendarContract.Events.DTSTART + " asc";

      Cursor cEvents = cr.query(uEvents, projection, selection, selArgs, orderBy);

      // Map of events (Date string -> ID)
      HashMap<String, Long> mapOfEvents = new HashMap<>();

      if (cEvents != null && cEvents.getCount() > 0) {
         while (cEvents.moveToNext()) {
            String day = cEvents.getString(1);
            long id = cEvents.getLong(0);
            mapOfEvents.put(day, id);
         }
         cEvents.close();
      }

      return mapOfEvents;
   }

   @Override
   protected void onProgressUpdate(Void... values) {
   }

   @Override
   protected void onPostExecute(Integer integer) {
      mOnEventRefreshListener.onEventsRefreshed(integer);
   }

   /**
    * Registers a callback, to be triggered when the loading is finished.
    */
   public void setOnEventRefreshedListener(OnEventRefreshListener listener) {
      if (listener == null) {
         listener = sDummyListener;
      }

      mOnEventRefreshListener = listener;
   }

   /**
    * A dummy no-op callback for use when there is no other listener set.
    */
   private static OnEventRefreshListener sDummyListener = new OnEventRefreshListener() {
      @Override
      public void onEventsRefreshed(int count) {
      }
   };

   /**
    * A callback interface used to listen for program refreshes.
    */
   public interface OnEventRefreshListener {
      /**
       * Called when the program was refreshed.
       */
      void onEventsRefreshed(int count);
   }
}
