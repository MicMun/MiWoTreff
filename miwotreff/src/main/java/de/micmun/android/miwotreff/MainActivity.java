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

import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;

import java.io.File;

import de.micmun.android.miwotreff.utils.ContextActionMode;
import de.micmun.android.miwotreff.utils.DBConstants;
import de.micmun.android.miwotreff.utils.JSONBackupRestore;
import de.micmun.android.miwotreff.utils.LoaderListener;
import de.micmun.android.miwotreff.utils.ProgramLoader;
import de.micmun.android.miwotreff.utils.SpecialCursorAdapter;

/**
 * Main Activity of the app.
 *
 * @author MicMun
 * @version 1.0, 18.01.2013
 */
public class MainActivity extends ListActivity implements LoaderListener,
      LoaderManager.LoaderCallbacks<Cursor> {
   private static final String TAG = "MiWoTreff";
   private static final int ACTIVITY_EDIT = 1;
   private SpecialCursorAdapter mAdapter;
   private MenuItem btnRefresh = null;
   private ContextActionMode cma;
   private boolean mustRefresh = false;

   /**
    * @see android.app.ListActivity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mAdapter = new SpecialCursorAdapter(this, null);
      setListAdapter(mAdapter);
      ListView lv = getListView();
      lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

      getLoaderManager().initLoader(0, null, this);

      cma = new ContextActionMode(this, lv);
      lv.setMultiChoiceModeListener(cma);
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
      if (mustRefresh) {
         refreshData();
         mustRefresh = false;
      }
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
            refreshData();
            return true;
         case R.id.menu_export:
            backupData();
            return true;
         case R.id.menu_import:
            restoreData();
            return true;
         case R.id.menu_delete:
            deleteOld();
            return true;
         default:
            return super.onOptionsItemSelected(item);
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
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      cma.onSaveInstanceState(outState);
   }

   /**
    * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
    */
   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      cma.onRestoreInstanceState(savedInstanceState);
   }

   /**
    * Refresh the data from the website.
    */
   private void refreshData() {
      ProgramLoader pl = new ProgramLoader(this, btnRefresh);
      pl.addLoaderListener(this);
      pl.execute(new Void[]{});
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
         AppMsg.makeText(this, R.string.no_restore, AppMsg.STYLE_INFO).show();
         return;
      }
      String[] fileNames = new String[files.length];
      // build the file names
      for (int i = 0; i < fileNames.length; ++i) {
         fileNames[i] = files[i].getName();
      }
      // dialog with files, click on file restore data from file.
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.menu_restore);
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
         for (int i = 5; i < files.length; ++i) {
            delFiles[i - 5] = files[i];
         }
         jbr.execute(delFiles);
      } else {
         AppMsg.makeText(this, R.string.no_del, AppMsg.STYLE_INFO).show();
      }
   }

   // **************************************************************************
   //   CursorLoader
   // **************************************************************************

   /**
    * @see android.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
    * android.os.Bundle)
    */
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
         mustRefresh = true;
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
