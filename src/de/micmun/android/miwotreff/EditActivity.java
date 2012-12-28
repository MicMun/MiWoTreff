/**
 * EditActivity.java
 *
 * Copyright 2012 by Michael Munzert
 */
package de.micmun.android.miwotreff;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for creating or editing a program entry.
 * Shows a non-editable Date-Field (edit) or a editable Date-Field (create) and
 * the fields topic and person.
 *
 * @author Michael Munzert
 * @version 1.0, 11.08.2012
 */
public class EditActivity
extends Activity
{
   private static final String TAG = "EditActivity";
   
   private EditText mThemaEdit; // topic input
   private EditText mPersonEdit; // person input
   private Long mRowId; // _id
   private EditText mDatumView; // Date
   private DbAdapter mDbHelper; // database
   
   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @SuppressLint ("NewApi")
   @Override
   protected void onCreate (Bundle savedInstanceState) {
      super.onCreate (savedInstanceState);
      mDbHelper = new DbAdapter (this);
      mDbHelper.open ();
      setContentView (R.layout.activity_edit);
      ActionBar ab = getActionBar();
      ab.setDisplayHomeAsUpEnabled(true);
      
      mThemaEdit = (EditText)findViewById (R.id.edit_thema);
      mPersonEdit = (EditText)findViewById (R.id.edit_person);
      mDatumView = (EditText)findViewById (R.id.edit_datum);
      
      mRowId = savedInstanceState == null ? null : 
         (Long)savedInstanceState.getSerializable (DbAdapter.KEY_ROWID);
      if (mRowId == null) {
         Bundle extras = getIntent ().getExtras ();
         if (extras != null) {
            mRowId = extras.getLong (DbAdapter.KEY_ROWID);
         }
      }
      populateFields();
   }
   
   /**
    * Shows the error message.
    * 
    * @param  msg
    *         error message.
    */
   private void showError(String msg) {
      LayoutInflater inflater = getLayoutInflater();
      View layout = inflater.inflate
      (R.layout.error_main, (ViewGroup) findViewById(R.id.error_root));
      
      // ImageView image = (ImageView) layout.findViewById(R.id.error_icon);
      TextView text = (TextView) layout.findViewById(R.id.error_msg);
      text.setText(msg);
      
      Toast toast = new Toast(getApplicationContext());
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.setDuration(Toast.LENGTH_LONG);
      toast.setView(layout);
      toast.show();
   }
   
   /**
    * Sets the fields with the values to edit or with default values.
    */
   @SuppressWarnings("deprecation")
   private void populateFields()
   {
      if (mRowId != null) {
         // actual values
         try {
            Cursor note = mDbHelper.fetchEntry(mRowId);
            startManagingCursor(note);
            
            if (note != null) {
               mThemaEdit.setText(note.getString(note.getColumnIndexOrThrow
                                                 (DbAdapter.KEY_THEMA)));
               mPersonEdit.setText(note.getString(note.getColumnIndexOrThrow 
                                                  (DbAdapter.KEY_PERSON)));
               Long d = note.getLong(note.getColumnIndexOrThrow 
                                     (DbAdapter.KEY_DATUM));
               mDatumView.setText(DbAdapter.getDateString(d));
               mDatumView.setEnabled(false);
            }
         } catch (SQLException e) {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = getResources().getString
            (R.string.error_sql) + e.getLocalizedMessage();
            showError(msg);
         }
      } else {
         // Default values
         String d = DbAdapter.getDateString(new Date().getTime());
         String t = "Noch offen";
         String p = "Noch offen";
         mDatumView.setText(d);
         mThemaEdit.setText(t);
         mPersonEdit.setText(p);
      }
   }
   
   /**
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState(outState);
      saveState();
      outState.putSerializable(DbAdapter.KEY_ROWID, mRowId);
   }
   
   /**
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_edit, menu);
      return true;
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
         case R.id.menu_save:
            saveState();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
   
   /**
    * @see android.app.Activity#onPause()
    */
   @Override
   protected void onPause()
   {
      super.onPause();
   }
   
   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   protected void onResume()
   {
      super.onResume();
      populateFields();
   }
   
   @Override
   protected void onDestroy() {
      super.onDestroy();
      mDbHelper.close();
   }
   
   /**
    * Saves the data in the database (insert or update).
    */
   private void saveState()
   {
      String thema = mThemaEdit.getText().toString();
      String person = mPersonEdit.getText().toString();
      String datum = mDatumView.getText().toString();
      
      if (mRowId == null) {
         long id = mDbHelper.createEntry(DbAdapter.getDateFromString(datum), 
                                         thema, person);
         if (id > 0) {
            mRowId = id;
         } else {
            String msg = getResources().getString(R.string.entry_exits);
            Log.e(TAG, msg);
            showError(msg);
         }
      } else {
         mDbHelper.updateEntry(mRowId, thema, person);
      }
   }
}
