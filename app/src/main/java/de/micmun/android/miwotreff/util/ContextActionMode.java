/**
 * Copyright 2015 MicMun
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU >General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or >
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see >http://www.gnu.org/licenses/.
 */
package de.micmun.android.miwotreff.util;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBProvider;
import de.micmun.android.miwotreff.recyclerview.Program;

/**
 * Class for handling the contextual action bar.
 *
 * @author Michael Munzert
 * @version 1.2, 08.01.17
 */
public class ContextActionMode implements ActionMode.Callback {
   private final Activity mActivity;
   private long mId;
   private String selMsgFormat;
   private ActionMode mode;
   private View view;

   private DBProvider mDbProvider;

   /**
    * Creates a new ContextActionMode.
    *
    * @param context MainActivity.
    * @param view    selected view.
    * @param id      ID of the selected program.
    */
   public ContextActionMode(Activity context, View view, long id) {
      mId = id;
      mActivity = context;
      this.view = view;
      selMsgFormat = mActivity.getResources().getString(R.string.title_count_selected);
   }

   @Override
   public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      this.mode = mode;
      mDbProvider = DBProvider.getInstance(mActivity);
      // Inflate a menu resource providing context menu items
      MenuInflater inflater = mode.getMenuInflater();
      inflater.inflate(R.menu.context_menu, menu);
      mode.setTitle(selMsgFormat);

      view.setSelected(true);

      return true;
   }

   /**
    * Closes the action mode.
    */
   public void closeMode() {
      view.setSelected(false);
      mode.finish();
   }

   @Override
   public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
   }

   @Override
   public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      switch (item.getItemId()) {
         case R.id.addToCal:
            add2Cal(mId);
            mode.finish();
            return true;
         case R.id.share:
            showShareIntent(mId);
            mode.finish();
            return true;
         case R.id.home:
            mode.finish();
            return true;
      }
      return false;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      mDbProvider.close();
      mDbProvider = null;
      view.setSelected(false);
   }

   /**
    * Opens a standard share intent.
    *
    * @param id the ID of the entry to share.
    */
   private void showShareIntent(long id) {
      /* get data */
      String selection = DBConstants._ID + " = ?";
      String[] selectionArgs = {String.valueOf(id)};

      List<Program> plist = mDbProvider.queryProgram(selection, selectionArgs, null);

      if (plist.size() == 0) {
         return;
      }
      Program p = plist.get(0);

      /* date */
      long timestamp = p.getDate();
      Calendar then = GregorianCalendar.getInstance();
      then.setTimeInMillis(timestamp);
      Calendar now = GregorianCalendar.getInstance();

      /* topic */
      String topic = p.getTopic();

      String text;
      /* today */
      if (then.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
            && then.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            && then.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
         text = mActivity.getString(R.string.shareText_today, topic);
      } else { /* other day than today */
         SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
         if (then.compareTo(now) == -1) { /* other day in the past */
            text = mActivity.getString(
                  R.string.shareText_past, topic, df.format(then.getTime()));
         } else { /* other day in the future */
            text = mActivity.getString(
                  R.string.shareText_future, topic, df.format(then.getTime()));
         }
      }

      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_TEXT, text);
      shareIntent.setType("text/plain");
      mActivity.startActivity(Intent.createChooser(
            shareIntent, mActivity.getResources().getText(R.string.shareWith)));
   }

   /**
    * Adds the entry to the calendar.
    *
    * @param id ID of the entry.
    */
   private void add2Cal(long id) {
      /* get data */
      String selection = DBConstants._ID + " = ?";
      String[] selectionArgs = {String.valueOf(id)};

      List<Program> plist = mDbProvider.queryProgram(selection, selectionArgs, null);

      if (plist.size() == 0) {
         return;
      }
      Program p = plist.get(0);

      // Date and time of the calendar entry
      long d = p.getDate();
      GregorianCalendar start = new GregorianCalendar();
      GregorianCalendar end = new GregorianCalendar();
      start.setTimeInMillis(d);
      start.set(GregorianCalendar.HOUR_OF_DAY, 19);
      start.set(GregorianCalendar.MINUTE, 30);
      end.setTimeInMillis(d);
      end.set(GregorianCalendar.HOUR_OF_DAY, 21);

      // title
      String title = mActivity.getResources().getString(R.string.cal_prefix) + " " + p.getTopic();

      // location
      String loc = mActivity.getResources().getString(R.string.cal_loc);

      // Description
      String desc = mActivity.getResources().getString(R.string.app_name);

      // Calendar: Insert per Intent
      Intent intent = new Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                  start.getTimeInMillis())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                  end.getTimeInMillis())
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
            .putExtra(CalendarContract.Events.AVAILABILITY,
                  CalendarContract.Events.AVAILABILITY_BUSY)
            .putExtra(CalendarContract.Events.DESCRIPTION, desc);
      mActivity.startActivity(intent);
   }
}
