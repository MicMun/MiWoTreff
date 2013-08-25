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
package de.micmun.android.miwotreff;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.utils.BackupActionProvider;
import de.micmun.android.miwotreff.utils.DBConstants;
import de.micmun.android.miwotreff.utils.LoaderListener;
import de.micmun.android.miwotreff.utils.ProgramLoader;
import de.micmun.android.miwotreff.utils.SpecialCursorAdapter;
import de.micmun.android.miwotreff.utils.UndoBarController;

/**
 * Main Activity of the app.
 *
 * @author MicMun
 * @version 1.0, 18.01.2013
 */
public class MainActivity extends ListActivity implements LoaderListener,
      UndoBarController.UndoListener, LoaderManager.LoaderCallbacks<Cursor> {
   private static final String TAG = "MiWoTreff";
   private static final int ACTIVITY_EDIT = 1;
   private SpecialCursorAdapter mAdapter;
   private MenuItem btnRefresh = null;
   private UndoBarController mUndoBarController;
   private String tmpDelDatum;
   private String tmpDelThema;
   private String tmpDelPerson;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mAdapter = new SpecialCursorAdapter(this, null);
      setListAdapter(mAdapter);
      getLoaderManager().initLoader(0, null, this);

      registerForContextMenu(getListView());

      mUndoBarController = new UndoBarController(findViewById(R.id.undobar),
            this);
   }

   /**
    * @see android.app.ListActivity#onDestroy()
    */
   @Override
   public void onDestroy() {
      super.onDestroy();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.activity_main, menu);
      btnRefresh = menu.findItem(R.id.menu_refresh);
      // BackupActionProvider for backup/restore
      MenuItem bi = menu.findItem(R.id.menu_export);
      BackupActionProvider bap = new BackupActionProvider(this);
      bap.setActivity(this);
      bi.setActionProvider(bap);
      return true;
   }

   /**
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.menu_search:
            onSearchRequested();
            return true;
         case R.id.menu_refresh:
            ProgramLoader pl = new ProgramLoader(this, btnRefresh);
            pl.addLoaderListener(this);
            pl.execute(new Void[]{});
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
    * android.view.View, android.view.ContextMenu.ContextMenuInfo)
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenu.ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.context_menu, menu);
   }

   /**
    * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

      switch (item.getItemId()) {
         case R.id.addToCal: // Add to google calendar
            add2Cal(info);
            return true;
         case R.id.delItem: // Delete Item
            delItem(info);
            return true;
         default:
            return super.onContextItemSelected(item);
      }
   }

   /**
    * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
    * android.view.View, int, long)
    */
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);
      // Edit the entry
      Intent i = new Intent(this, EditActivity.class);
      i.putExtra(DBConstants._ID, id);
      startActivityForResult(i, ACTIVITY_EDIT);
   }

   /**
    * @see android.app.Activity#onActivityResult(int, int,
    * android.content.Intent)
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode,
                                   Intent intent) {
      super.onActivityResult(requestCode, resultCode, intent);
   }

   /**
    * @see de.micmun.android.miwotreff.utils.LoaderListener#update(int c)
    */
   @Override
   public void update(int c) {
      String msg = String.format(getResources()
            .getString(R.string.load_success), c);
      AppMsg.makeText(this, msg, AppMsg.STYLE_INFO).show();
   }

   /**
    * Adds the entry to the calendar.
    *
    * @param info Info about the entry.
    */
   private void add2Cal(AdapterContextMenuInfo info) {
//        Cursor c = mDbHelper.fetchEntry(info.id);
      // Date and time of the calendar entry
//        long d = c.getLong(1);
//        GregorianCalendar start = new GregorianCalendar();
//        GregorianCalendar end = new GregorianCalendar();
//        start.setTimeInMillis(d);
//        start.set(GregorianCalendar.HOUR_OF_DAY, 19);
//        start.set(GregorianCalendar.MINUTE, 30);
//        end.setTimeInMillis(d);
//        end.set(GregorianCalendar.HOUR_OF_DAY, 21);
//
//        // title
//        String title = getResources().getString(R.string.cal_prefix) + " "
//                + c.getString(2);
//
//        // location
//        String loc = getResources().getString(R.string.cal_loc);
//
//        // Calendar: Insert per Intent
//        Intent intent = new Intent(Intent.ACTION_INSERT)
//                .setData(Events.CONTENT_URI)
//                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
//                        start.getTimeInMillis())
//                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
//                        end.getTimeInMillis()).putExtra(Events.TITLE, title)
//                .putExtra(Events.EVENT_LOCATION, loc)
//                .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
//        startActivity(intent);
   }

   /**
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      mUndoBarController.onSaveInstanceState(outState);
   }

   /**
    * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
    */
   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      mUndoBarController.onRestoreInstanceState(savedInstanceState);
   }

   /**
    * Deletes an item from database and list.
    *
    * @param info Info about the entry.
    */
   private void delItem(AdapterContextMenuInfo info) {
        /* Store temporarily */
//        Cursor c = mDbHelper.fetchEntry(info.id);
//        tmpDelDatum = DbHelper.getDateString(c.getLong(1));
//        tmpDelThema = c.getString(2);
//        tmpDelPerson = c.getString(3);
//        c.close();
//
//        if (!mDbHelper.deleteEntry(info.id)) {
//            String msg = String.format(
//                    getResources().getString(R.string.error_delItem), info.id);
//            Log.e(TAG, msg);
//            AppMsg.makeText(this, msg, AppMsg.STYLE_ALERT).show();
//            return;
//        } else { // Successfully deleted
//            mUndoBarController.showUndoBar(false,
//                    getString(R.string.undobar_entry_deleted), null);
//            fillData();
//            getListView().setOnTouchListener(new View.OnTouchListener() {
//                /**
//                 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
//                 */
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    mUndoBarController.hideUndoBar(false);
//                    return false;
//                }
//            });
//        }
   }

   /**
    * @see de.micmun.android.miwotreff.utils.UndoBarController.UndoListener#onUndo(android.os.Parcelable)
    */
   @Override
   public void onUndo(Parcelable token) {
//        mDbHelper.createEntry(DbHelper.getDateFromString(tmpDelDatum),
//                tmpDelThema, tmpDelPerson);
//        fillData();
   }

   // *********************************************************************************************
   //   CursorLoader
   // *********************************************************************************************

   /**
    * @see android.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
    */
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      return new CursorLoader(this, DBConstants.TABLE_CONTENT_URI, null, null, null, null);
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      Cursor old = mAdapter.swapCursor(data);
      if (old != null) {
         old.close();
      }
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      Cursor old = mAdapter.swapCursor(null);
      if (old != null) {
         old.close();
      }
   }
}
