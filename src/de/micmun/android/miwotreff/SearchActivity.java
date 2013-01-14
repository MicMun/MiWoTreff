/**
 * SearchActivity.java
 *
 * Copyright 2012 by Michael Munzert
 */
package de.micmun.android.miwotreff;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.utils.DbAdapter;
import de.micmun.android.miwotreff.utils.SpecialCursorAdapter;

/**
 * Shows the Search Result.
 *
 * @author Michael Munzert
 * @version 1.0, 11.08.2012
 */
public class SearchActivity
extends ListActivity
{
	private final String TAG = "MiWoTreff.SearchActivity";
   private DbAdapter mDbHelper; // Database Helper
   
   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {      
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      
      mDbHelper = new DbAdapter(this);
      try {
         mDbHelper.open();
      } catch (SQLException s) {
         Log.e(TAG, s.getLocalizedMessage());
         AppMsg.makeText(this, R.string.db_open_error, 
                         AppMsg.STYLE_ALERT).show();
         return;
      }
      
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
    * @param  intent
    *         Intent of the context.
    */
   private void handleIntent(String search) {
      fillData(search);
   }
   
   /**
    * @see android.app.ListActivity#onPause()
    */
   @Override
   public void onPause() {  
      super.onPause ();
   }
   
   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   public void onResume() {
      super.onResume ();
   }
   
   /**
    * Fills Data from Database in List.
    */
   private void fillData(String search) {
      Cursor entryCursor = mDbHelper.fetchAllEntries(search);
      SpecialCursorAdapter adapter = new SpecialCursorAdapter(this,entryCursor);
      setListAdapter(adapter);
   }
   
   @Override
   public void onBackPressed() {
      // app icon in action bar clicked; go home
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      super.onBackPressed();
   }
}
