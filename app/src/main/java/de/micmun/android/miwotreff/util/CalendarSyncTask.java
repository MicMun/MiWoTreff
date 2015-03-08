package de.micmun.android.miwotreff.util;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.text.format.Time;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import de.micmun.android.miwotreff.BaseActivity;
import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * BESCHREIBUNG.
 *
 * @author MicMun
 * @version 1.0, 27.02.15
 */
public class CalendarSyncTask extends AsyncTask<Void, Integer, Integer> {
   private final String TAG = "MiWoTreff.ProgramLoader";
   private final String EVENT_LOC;
   private final String EVENT_DESC;
   private final Context mCtx;
   private final CalendarInfo mCalInfo;
   private final long TODAY_MILLIS;
   private int counter;
   private int gesamt;
   private ProgressDialog mProgressBar;

   public CalendarSyncTask(Context ctx, CalendarInfo calendarInfo) {
      mCtx = ctx;
      mCalInfo = calendarInfo;
      counter = 0;
      gesamt = 0;

      // Constant values
      EVENT_LOC = mCtx.getString(R.string.cal_loc);
      EVENT_DESC = mCtx.getString(R.string.app_name);
      TODAY_MILLIS = Calendar.getInstance().getTime().getTime();

      mProgressBar = new ProgressDialog(mCtx, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
      mProgressBar.setIndeterminate(false);
      mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mProgressBar.setCancelable(false);
   }

   @Override
   protected Integer doInBackground(Void... params) {
      Cursor cProg = getProgramToSync();
      if (cProg == null || cProg.getCount() <= 0) { // no data to sync
         if (cProg != null)
            cProg.close();
         return 0;
      }
      gesamt = cProg.getCount();
      publishProgress(counter, gesamt);

      HashMap<String, Long> eventMap = getEvents();

      while (cProg.moveToNext()) {
         long date = cProg.getLong(0);
         String topic = cProg.getString(1);
         String person = cProg.getString(2);

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
         publishProgress(counter, gesamt);
      }
      cProg.close();

      return counter;
   }

   /**
    * Returns a cursor with program entries.
    *
    * @return a cursor with program entries.
    */
   private Cursor getProgramToSync() {
      ContentResolver cr = mCtx.getContentResolver();
      Uri uProg = Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI, DBConstants.SYNC_QUERY);
      String[] selArgs = new String[]{String.valueOf(TODAY_MILLIS)};

      return cr.query(uProg, null, null, selArgs, null);
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
      Time t = new Time();
      t.setToNow();
      String dtstart = Long.toString(t.toMillis(false));
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
   protected void onProgressUpdate(Integer... values) {
      mProgressBar.setProgress(values[0]);
      if (!mProgressBar.isShowing()) {
         mProgressBar.setMax(values[1]);
         mProgressBar.show();
      }
   }

   @Override
   protected void onPostExecute(Integer integer) {
      if (mProgressBar.isShowing())
         mProgressBar.hide();
      // result message
      String resultMsg = String.format(mCtx.getString(R.string.sync_result), integer);
      CustomToast.makeText((BaseActivity) mCtx, resultMsg, CustomToast.TYPE_INFO).show();
   }
}
