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
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;

import java.util.Date;

import de.micmun.android.miwotreff.utils.DBConstants;
import de.micmun.android.miwotreff.utils.DBDateUtility;
import de.micmun.android.miwotreff.utils.SpecialCursorAdapter;
import de.micmun.android.miwotreff.utils.UndoBarController;

/**
 * Shows the Search Result.
 *
 * @author MicMun
 * @version 2.0, 18.01.2013
 */
public class SearchActivity
      extends ListActivity
      implements UndoBarController.UndoListener,
      LoaderManager.LoaderCallbacks<Cursor> {
   private static final int SEARCH_DATE = 0;
   private static final int SEARCH_OTHERS = 1;
   private static final String KEY_SEARCH = "search";
   private final String TAG = "MiWoTreff.SearchActivity";
   private SpecialCursorAdapter mAdapter;

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      getActionBar().setDisplayHomeAsUpEnabled(true);

      new UndoBarController(findViewById(R.id.undobar),
            this);

      mAdapter = new SpecialCursorAdapter(this, null);
      setListAdapter(mAdapter);

      Intent intent = getIntent();
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         handleIntent(query);
      }
   }

   /**
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * @see android.app.Activity#onNewIntent(android.content.Intent)
    */
   @Override
   protected void onNewIntent(Intent intent) {
      setIntent(intent);
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         handleIntent(query);
      }
   }

   /**
    * Handle the intent with search.
    *
    * @param search Intent of the context.
    */
   private void handleIntent(String search) {
      Bundle b = new Bundle();
      b.putString(KEY_SEARCH, search);
      if (search.matches("^[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2,4}")) {
         Log.d(TAG, "Search: " + search);
         getLoaderManager().initLoader(SEARCH_DATE, b, this);
      } else {
         getLoaderManager().initLoader(SEARCH_OTHERS, b, this);
      }
   }

   /**
    * @see android.app.ListActivity#onPause()
    */
   @Override
   public void onPause() {
      super.onPause();
   }

   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   public void onResume() {
      super.onResume();
   }

   @Override
   public void onBackPressed() {
      // app icon in action bar clicked; go home
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      super.onBackPressed();
   }

   /**
    * @see de.micmun.android.miwotreff.utils.UndoBarController.UndoListener#onUndo(android.os.Parcelable)
    */
   @Override
   public void onUndo(Parcelable token) {
      // Do nothing, only because of not showing undo message
   }

   // *********************************************************************************************
   //   CursorLoader
   // *********************************************************************************************

   /**
    * @see android.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
    */
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      Uri uri;
      String sel;
      String[] selArgs;

      switch (id) {
         case SEARCH_DATE:
            Date d = DBDateUtility.getDateFromString(args.getString
                  (KEY_SEARCH));
            uri = DBConstants.TABLE_CONTENT_URI;
            sel = DBConstants.KEY_DATUM + " = ?";
            selArgs = new String[]{String.valueOf(d.getTime())};
            return new CursorLoader(this, uri, null, sel, selArgs, null);
         case SEARCH_OTHERS:
            String s = args.getString(KEY_SEARCH);
            uri = DBConstants.TABLE_CONTENT_URI;
            sel = "UPPER(" + DBConstants.KEY_THEMA + ") LIKE ? OR UPPER(" +
                  DBConstants.KEY_PERSON + ") LIKE ?";
            selArgs = new String[]{"%" + s + "%", "%" + s + "%"};
            return new CursorLoader(this, uri, null, sel, selArgs, null);
      }
      return null;
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
