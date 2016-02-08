/*
 * Copyright 2015-2016 MicMun
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

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.service.AlarmConfiger;
import de.micmun.android.miwotreff.util.CalendarInfo;
import de.micmun.android.miwotreff.util.CalendarSyncHelper;
import de.micmun.android.miwotreff.util.CalendarSyncTask;
import de.micmun.android.miwotreff.util.ContextActionMode;
import de.micmun.android.miwotreff.util.CustomToast;
import de.micmun.android.miwotreff.util.JSONBackupRestore;
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
      implements LoaderManager.LoaderCallbacks<Cursor>,
      AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
   private static final int ACTIVITY_EDIT = 1;

   private SpecialCursorAdapter mAdapter;
   private String lastDate;

   private MenuItem mMenuItemRefresh;
   private SwipeRefreshLayout mSwipeLayout;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mAdapter = new SpecialCursorAdapter(this, null);

      ListView lv = (ListView) findViewById(R.id.progListView);
      lv.setAdapter(mAdapter);
      lv.setOnItemClickListener(this);
      lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
      ContextActionMode cma = new ContextActionMode(this, lv);
      lv.setMultiChoiceModeListener(cma);

      // Swipe Layout
      mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
      mSwipeLayout.setOnRefreshListener(this);
      mSwipeLayout.setColorSchemeResources(
            R.color.primary,
            R.color.accent);

      // load data
      getLoaderManager().initLoader(0, null, this);

      // set alarm for update service
      AlarmConfiger.setAlarmService(this);
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
      mMenuItemRefresh = menu.findItem(R.id.action_refresh); // refresh action
      // search action
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
      SearchView searchView = (SearchView) MenuItemCompat.getActionView(
            menu.findItem(R.id.action_search));
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      final int id = item.getItemId();

      switch (id) {
         case R.id.action_refresh:
            onRefresh();
            return true;
         case R.id.action_sync:
            // Check permission for calendar
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                  new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                  new PermissionsResultAction() {
                     @Override
                     public void onGranted() {
                        syncWithCal();
                     }

                     @Override
                     public void onDenied(String permission) {
                        CustomToast.makeText(MainActivity.this, getString(R.string.sync_perm_error),
                              CustomToast.TYPE_ERROR).show();
                     }
                  });
            return true;
      }

      // Check permission for storage
      PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            new PermissionsResultAction() {

               @Override
               public void onGranted() {
                  switch (id) {
                     case R.id.action_backup:
                        backupData();
                        break;
                     case R.id.action_restore:
                        restoreData();
                        break;
                     case R.id.action_clean:
                        deleteOld();
                        break;
                  }
               }

               @Override
               public void onDenied(String permission) {
                  CustomToast.makeText(MainActivity.this, getString(R.string.error_storage_perm),
                        CustomToast.TYPE_ERROR).show();
               }
            });
      return id == R.id.action_backup || id == R.id.action_restore || id == R.id.action_clean ||
            super.onOptionsItemSelected(item);

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
   }

   /**
    * Backup the data.
    */
   private void backupData() {
      // start loading: show the indicator and disable the "refresh" menu icon
      mSwipeLayout.setRefreshing(true);

      JSONBackupRestore jbr = new JSONBackupRestore(this,
            JSONBackupRestore.TYPE_BACKUP);
      final BaseActivity context = this;

      jbr.setOnRefreshListener(new JSONBackupRestore.OnDataRefreshListener() {
         @Override
         public void onDataRefreshed(int rc, String msg) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            if (rc == 0) {
               // Show success message
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
            } else {
               CustomToast.makeText(context, msg, CustomToast.TYPE_ERROR).show();
            }
         }
      });
      jbr.execute();
   }

   /**
    * Restores the data.
    */
   private void restoreData() {
      final JSONBackupRestore jbr = new JSONBackupRestore(this,
            JSONBackupRestore.TYPE_RESTORE);
      final BaseActivity context = this;

      jbr.setOnRefreshListener(new JSONBackupRestore.OnDataRefreshListener() {
         @Override
         public void onDataRefreshed(int rc, String msg) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            if (rc == 0) {
               // Show success message
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
            } else {
               CustomToast.makeText(context, msg, CustomToast.TYPE_ERROR).show();
            }
         }
      });

      // get files to restore
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
            // start loading: show the indicator and disable the "refresh" menu icon
            mSwipeLayout.setRefreshing(true);
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
      final BaseActivity context = this;

      jbr.setOnRefreshListener(new JSONBackupRestore.OnDataRefreshListener() {
         @Override
         public void onDataRefreshed(int rc, String msg) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            if (rc == 0) {
               // Show success message
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
            } else {
               CustomToast.makeText(context, msg, CustomToast.TYPE_ERROR).show();
            }
         }
      });

      final File[] files = jbr.getBackupFiles();
      if (files.length > 5) {
         File[] delFiles = new File[files.length - 5];
         System.arraycopy(files, 5, delFiles, 0, files.length - 5);
         // start loading: show the indicator and disable the "refresh" menu icon
         mSwipeLayout.setRefreshing(true);
         jbr.execute(delFiles);
      } else {
         CustomToast.makeText(this, getString(R.string.no_del), CustomToast.TYPE_INFO).show();
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode,
                                          @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
      PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
   }

   /**
    * Sync the program with a calendar.
    */
   private void syncWithCal() {
      final BaseActivity context = this;

      final CalendarSyncTask.OnEventRefreshListener listener = new CalendarSyncTask.OnEventRefreshListener() {
         @Override
         public void onEventsRefreshed(int count) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            // result message
            String resultMsg = String.format(context.getString(R.string.sync_result), count);
            CustomToast.makeText(context, resultMsg, CustomToast.TYPE_INFO).show();
         }
      };

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
               // start loading: show the indicator
               final int index = which;
               AlertDialog.Builder builder = new AlertDialog.Builder(context);
               builder.setTitle(R.string.sync_confim_title);
               builder.setMessage(R.string.sync_confim_message);
               builder.setNegativeButton(R.string.sync_confirm_cancel, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                     //Do nothing
                  }
               });
               builder.setPositiveButton(R.string.sync_confirm_yes, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                     mSwipeLayout.setRefreshing(true);
                     CalendarSyncHelper.syncCalendar(context, calendars.get(index), listener);
                  }
               });
               dialog.cancel();
               builder.show();
            }
         });
         builder.show();
      } else { // only one calendar -> sync with that
         // start loading: show the indicator
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.sync_confim_title);
         builder.setMessage(R.string.sync_confim_message);
         builder.setNegativeButton(R.string.sync_confirm_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               //Do nothing
            }
         });
         builder.setPositiveButton(R.string.sync_confirm_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               mSwipeLayout.setRefreshing(true);
               CalendarSyncHelper.syncCalendar(context, calendars.get(0), listener);
            }
         });
         builder.show();
      }
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
   protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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
         onRefresh();
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

   /* =========================================================================================== */
   /*  Swipe Refresh Layout                                                                       */
   /* =========================================================================================== */

   @Override
   public void onRefresh() {
      // start loading: show the indicator and disable the "refresh" menu icon
      mSwipeLayout.setRefreshing(true);
      if (mMenuItemRefresh != null)
         mMenuItemRefresh.setEnabled(false);
      // load new data
      ProgramLoader pl = new ProgramLoader(this, lastDate);
      pl.execute();
      final BaseActivity context = this;
      pl.setOnProgramRefreshedListener(new ProgramLoader.OnProgramRefreshListener() {
         @Override
         public void onProgramRefreshed(int count) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            if (mMenuItemRefresh != null)
               mMenuItemRefresh.setEnabled(true);
            if (count != -1) {
               String msg = String.format(getString(R.string.load_success), count);
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
            }
         }
      });

   }
}
