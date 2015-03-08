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
package de.micmun.android.miwotreff;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.util.CalendarInfo;
import de.micmun.android.miwotreff.util.CalendarSyncHelper;
import de.micmun.android.miwotreff.util.ContextActionMode;
import de.micmun.android.miwotreff.util.CustomToast;
import de.micmun.android.miwotreff.util.JSONBackupRestore;
import de.micmun.android.miwotreff.util.LoaderListener;
import de.micmun.android.miwotreff.util.ProgramLoader;
import de.micmun.android.miwotreff.util.SpecialCursorAdapter;

/**
 * Main activity for miwotreff.
 *
 * @author MicMun
 * @version 1.0, 14.01.2015
 */
public class MainActivity
      extends BaseActivity
      implements LoaderManager.LoaderCallbacks<Cursor>, LoaderListener,
      AdapterView.OnItemClickListener {
   private static final String TAG = "MainActivity"; // for logging

   private static final int ACTIVITY_EDIT = 1;
   private static final int ACTIVITY_ACCOUNT = 2;
   private SpecialCursorAdapter mAdapter;
   private String lastDate;
   private ContextActionMode cma;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mAdapter = new SpecialCursorAdapter(this, null);

      ListView lv = (ListView) findViewById(R.id.progListView);
      lv.setAdapter(mAdapter);
      lv.setOnItemClickListener(this);
      lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
      cma = new ContextActionMode(this, lv);
      lv.setMultiChoiceModeListener(cma);

      getLoaderManager().initLoader(0, null, this);
   }

   /**
    * @see BaseActivity#getLayoutResource()
    */
   @Override
   protected int getLayoutResource() {
      return R.layout.activity_main;
   }


   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      switch (id) {
         case R.id.action_refresh:
            refreshData();
            return true;
         case R.id.action_search:
            onSearchRequested();
            return true;
         case R.id.action_sync:
            syncWithCal();
            return true;
         case R.id.action_backup:
            backupData();
            return true;
         case R.id.action_restore:
            restoreData();
            return true;
         case R.id.action_clean:
            deleteOld();
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   /**
    * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
    * android.view.View, int, long)
    */
   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      // Edit the entry
      Intent i = new Intent(this, EditActivity.class);
      i.putExtra(DBConstants._ID, id);
      startActivityForResult(i, ACTIVITY_EDIT);
   }

   /**
    * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
      super.onActivityResult(requestCode, resultCode, intent);

      if (requestCode == ACTIVITY_ACCOUNT && resultCode == 0) {

      }
   }

   /**
    * Refresh the data from the website.
    */
   private void refreshData() {
      ProgramLoader pl = new ProgramLoader(this, lastDate);
      pl.addLoaderListener(this);
      pl.execute();
   }

   /**
    * Backup the data.
    */
   private void backupData() {
      JSONBackupRestore jbr = new JSONBackupRestore(this,
            JSONBackupRestore.TYPE_BACKUP);
      jbr.execute();
   }

   /**
    * Restores the data.
    */
   private void restoreData() {
      final JSONBackupRestore jbr = new JSONBackupRestore(this,
            JSONBackupRestore.TYPE_RESTORE);
      final File[] files = jbr.getBackupFiles();
      if (files == null || files.length <= 0) {
         CustomToast.makeText(this, getString(R.string.no_restore), CustomToast.TYPE_INFO).show();
         return;
      }
      String[] fileNames = new String[files.length];
      // build the file names
      for (int i = 0; i < fileNames.length; ++i) {
         fileNames[i] = files[i].getName();
      }
      // dialog with files, click on file restore data from file.
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.action_restore);
      builder.setItems(fileNames, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            jbr.execute(files[which]);
         }
      });
      builder.show();
   }

   /**
    * Deletes old backup files.
    */
   private void deleteOld() {
      final JSONBackupRestore jbr = new JSONBackupRestore(this,
            JSONBackupRestore.TYPE_DELETE);
      final File[] files = jbr.getBackupFiles();
      if (files.length > 5) {
         File[] delFiles = new File[files.length - 5];
         System.arraycopy(files, 5, delFiles, 0, files.length - 5);
         jbr.execute(delFiles);
      } else {
         CustomToast.makeText(this, getString(R.string.no_del), CustomToast.TYPE_INFO).show();
      }
   }

   /**
    * Sync the program with a calendar.
    */
   private void syncWithCal() {
      final ArrayList<CalendarInfo> calendars = CalendarSyncHelper.getCalendars(this);
      if (calendars.size() <= 0) {
         CustomToast.makeText(this, getString(R.string.sync_no_calendars),
               CustomToast.TYPE_INFO).show();
      } else if (calendars.size() > 1) {
         // Array with calendar names
         String[] calNames = new String[calendars.size()];
         for (int i = 0; i < calNames.length; ++i) {
            calNames[i] = calendars.get(i).getDisplayName();
         }
         // Dialog with calendars to choose
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.sync_choose_title);
         builder.setItems(calNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               CalendarSyncHelper.syncCalendar(getApplicationContext(), calendars.get(which));
            }
         });
         builder.show();
      } else { // only one calendar -> sync with that
         CalendarSyncHelper.syncCalendar(this, calendars.get(0));
      }


      // TODO: Sync with choosen calendar
   }

   @Override
   public void update(int c) {
      String msg = String.format(getString(R.string.load_success), c);
      CustomToast.makeText(this, msg, CustomToast.TYPE_INFO).show();
   }

   /**
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
   }

   /**
    * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
    */
   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
   }

   /* =========================================================================================== */
   /*  Cursor loader                                                                              */
   /* =========================================================================================== */

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      return new CursorLoader(this, DBConstants.TABLE_CONTENT_URI, null,
            null, null, null);
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      Cursor old = mAdapter.swapCursor(data);
      if (old != null) {
         old.close();
      }
      // if no data -> mustRefresh true
      if (mAdapter.getCount() <= 0) {
         lastDate = DBDateUtility.getDateString(Calendar.getInstance().getTimeInMillis());
         refreshData();
      } else {
         lastDate = DBDateUtility.getDateString(((Cursor) mAdapter.getItem(0)).getLong(1));
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
