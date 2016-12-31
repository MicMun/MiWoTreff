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
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.db.DBProvider;
import de.micmun.android.miwotreff.recyclerview.DividerDecoration;
import de.micmun.android.miwotreff.recyclerview.Program;
import de.micmun.android.miwotreff.recyclerview.ProgramAdapter;
import de.micmun.android.miwotreff.recyclerview.RecyclerItemListener;
import de.micmun.android.miwotreff.service.AlarmConfiger;
import de.micmun.android.miwotreff.util.AppPreferences;
import de.micmun.android.miwotreff.util.CalendarInfo;
import de.micmun.android.miwotreff.util.CalendarSyncHelper;
import de.micmun.android.miwotreff.util.CalendarSyncTask;
import de.micmun.android.miwotreff.util.ContextActionMode;
import de.micmun.android.miwotreff.util.CustomToast;
import de.micmun.android.miwotreff.util.JSONBackupRestore;
import de.micmun.android.miwotreff.util.ProgramSaver;

/**
 * Main activity for miwotreff.
 *
 * @author MicMun
 * @version 1.3, 31.12.16
 */
public class MainActivity
      extends BaseActivity
      implements RecyclerItemListener.RecyclerTouchListener,
      SwipeRefreshLayout.OnRefreshListener {
   private static final int ACTIVITY_EDIT = 1;

   private DBProvider mDbProvider;
   private ProgramAdapter mAdapter;
   private RecyclerView mProgListView;
   private String lastDate;

   private MenuItem mMenuItemRefresh;
   private SwipeRefreshLayout mSwipeLayout;

   private AppPreferences mAppPreferences;

   @Override
   protected void onPause() {
      mAdapter.setProgramList(new ArrayList<Program>());
      mDbProvider.close();
      mDbProvider = null;
      super.onPause();
   }

   @Override
   protected void onResume() {
      super.onResume();
      mDbProvider = DBProvider.getInstance(this);
      loadData();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Adapter and list view
      mAdapter = new ProgramAdapter();
      mAdapter.setHasStableIds(true);
      mProgListView = (RecyclerView) findViewById(R.id.progListView);
      mProgListView.setAdapter(mAdapter);
      mProgListView.addOnItemTouchListener(new RecyclerItemListener(this, mProgListView, this));
      LinearLayoutManager llm = new LinearLayoutManager(this);
      llm.setOrientation(LinearLayoutManager.VERTICAL);
      mProgListView.setLayoutManager(llm);
      RecyclerView.ItemDecoration id = new DividerDecoration(ContextCompat
            .getDrawable(getApplicationContext(), R.drawable.recycler_divider));
      mProgListView.addItemDecoration(id);

      // Swipe Layout
      mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
      mSwipeLayout.setOnRefreshListener(this);
      mSwipeLayout.setColorSchemeResources(
            R.color.primary,
            R.color.accent);

      // app preferences
      mAppPreferences = new AppPreferences(getApplicationContext());

      // set alarm for update service, if auto sync is on
      if (mAppPreferences.isAutoSync()) {
         AlarmConfiger.setAlarmService(getApplicationContext());
      }
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
         case R.id.action_setting:
            Intent settingIntent = new Intent(this, SettingActivity.class);
            startActivity(settingIntent);
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
            // finished loading: remove the indicator
            mSwipeLayout.setRefreshing(false);
            if (rc == 0) {
               // Show success message
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
            } else {
               CustomToast.makeText(context, msg, CustomToast.TYPE_ERROR).show();
            }
            loadData();
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
            // start loading: show the indicator
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
      // user preference how much files to keep from deleting
      int numberToKeep = mAppPreferences.getNumberOfFilesToKeep();

      if (files != null && files.length > numberToKeep) {
         File[] delFiles = new File[files.length - numberToKeep];
         System.arraycopy(files, numberToKeep, delFiles, 0, files.length - numberToKeep);
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
   /*  Swipe Refresh Layout                                                                       */
   /* =========================================================================================== */

   /**
    * Returns <code>true</code>, if you are connected to the internet.
    *
    * @return <code>true</code>, if connected to the internet.
    */
   private boolean isOnline() {
      boolean ret = false;
      ConnectivityManager mConManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo ni = mConManager.getActiveNetworkInfo();

      if (ni != null && ni.isConnected() && !ni.isRoaming())
         ret = true;

      return ret;
   }

   @Override
   public void onRefresh() {
      if (!isOnline()) {
         CustomToast.makeText(this, getString(R.string.error_pl_noconnect),
               CustomToast.TYPE_ERROR).show();
         mSwipeLayout.setRefreshing(false);
         if (mMenuItemRefresh != null)
            mMenuItemRefresh.setEnabled(true);
         return;
      }

      // start loading: show the indicator and disable the "refresh" menu icon
      mSwipeLayout.setRefreshing(true);
      if (mMenuItemRefresh != null)
         mMenuItemRefresh.setEnabled(false);
      // load new data
      final BaseActivity context = this;
      ProgramSaver ps = new ProgramSaver(this);
      ps.setOnProgramRefreshedListener(new ProgramSaver.OnProgramRefreshListener() {
         @Override
         public void onProgramRefreshed(int countInsert, int countUpdate) {
            // finished loading: remove the indicator and enable the menu icon again
            mSwipeLayout.setRefreshing(false);
            if (mMenuItemRefresh != null)
               mMenuItemRefresh.setEnabled(true);
            if (countInsert != -1) {
               String msg = String.format(getString(R.string.load_success), countInsert, countUpdate);
               CustomToast.makeText(context, msg, CustomToast.TYPE_INFO).show();
               loadData();
            } else {
               String msg = getString(R.string.error_pl_fetch);
               CustomToast.makeText(context, msg, CustomToast.TYPE_ERROR).show();
            }
         }
      });
      String url = "http://www.mittwochstreff-muenchen.de/program/api/index.php?op=0&von=" +
            lastDate;
      Ion.with(this).load(url).asJsonArray().setCallback(ps);
   }

   ContextActionMode cma = null;

   @Override
   public void onClickItem(View v, int position) {
      if (cma != null) {
         cma.closeMode();
         cma = null;
      }
      // Edit the entry
      long id = mAdapter.getItemId(position);
      Intent i = new Intent(this, EditActivity.class);
      i.putExtra(DBConstants._ID, id);
      startActivityForResult(i, ACTIVITY_EDIT);
   }

   @Override
   public void onLongClickItem(View v, int position) {
      if (cma != null) {
         cma.closeMode();
         cma = null;
      }
      // Start action mode
      cma = new ContextActionMode(this, v, mAdapter.getItemId(position));
      v.startActionMode(cma);
   }

   /**
    * Loads the data from database in adapter an scroll to current wednesday.
    */
   private void loadData() {
      List<Program> programs = mDbProvider.queryProgram(null, null, null);
      mAdapter.setProgramList(programs);

      if (programs.size() > 0) {
         lastDate = mDbProvider.getLastDate();
         mProgListView.postDelayed(new Runnable() {
            @Override
            public void run() {
               mProgListView.smoothScrollToPosition(mDbProvider.getPosOfNextWednesday());
            }
         }, 300);
      } else {
         lastDate = DBDateUtility.getDateString(Calendar.getInstance().getTimeInMillis());
         onRefresh();
      }
   }
}
