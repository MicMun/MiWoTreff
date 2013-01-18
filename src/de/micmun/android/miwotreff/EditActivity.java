/**
 * EditActivity.java
 *
 * Copyright 2013 by MicMun
 */
package de.micmun.android.miwotreff;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.utils.DbAdapter;

/**
 * An Activity to edit an entry.
 * 
 * @author MicMun
 * @version 1.0, 18.01.2013
 * 
 */
public class EditActivity extends Activity
{
   private static final String TAG = "EditActivity";

   private EditText mThemaEdit; // topic input
   private EditText mPersonEdit; // person input
   private EditText mDatumView; // Date

   private Long mRowId; // _id

   private DbAdapter mDbHelper; // database

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit);

      mDbHelper = new DbAdapter(this);
      mDbHelper.open();

      mThemaEdit = (EditText) findViewById(R.id.edit_thema);
      mPersonEdit = (EditText) findViewById(R.id.edit_person);
      mDatumView = (EditText) findViewById(R.id.edit_datum);

      mRowId = savedInstanceState == null ? null : (Long) savedInstanceState
               .getSerializable(DbAdapter.KEY_ROWID);
      if (mRowId == null) {
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
            mRowId = extras.getLong(DbAdapter.KEY_ROWID);
         }
      }
      populateFields();

      /*
       * Done & Discard
       */
      // Inflate a "Done/Discard" custom action bar view.
      LayoutInflater inflater = (LayoutInflater) getActionBar()
               .getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
      final View customActionBarView = inflater
               .inflate(R.layout.actionbar_custom_view_done_discard, null);
      customActionBarView.findViewById(R.id.actionbar_done)
      .setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // "Done"
            saveState();
            quitActivity();
         }
      });
      customActionBarView.findViewById(R.id.actionbar_discard)
      .setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // "Discard"
            quitActivity();
            // finish(); //don't just finish()!
         }
      });

      // Show the custom action bar view and hide the normal Home icon and
      // title.
      final ActionBar actionBar = getActionBar();
      actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                                  ActionBar.DISPLAY_SHOW_CUSTOM
                                  | ActionBar.DISPLAY_SHOW_HOME
                                  | ActionBar.DISPLAY_SHOW_TITLE);
      actionBar.setCustomView(customActionBarView,
                              new ActionBar.LayoutParams(
                                                         ViewGroup.LayoutParams.MATCH_PARENT,
                                                         ViewGroup.LayoutParams.MATCH_PARENT));
   }

   /**
    * Sets the fields with the values to edit or with default values.
    */
   private void populateFields() {
      // actual values
      try {
         Cursor note = mDbHelper.fetchEntry(mRowId);

         if (note != null) {
            mThemaEdit.setText(note.getString(note
                                              .getColumnIndexOrThrow(DbAdapter.KEY_THEMA)));
            mPersonEdit.setText(note.getString(note
                                               .getColumnIndexOrThrow(DbAdapter.KEY_PERSON)));
            Long d = note.getLong(note
                                  .getColumnIndexOrThrow(DbAdapter.KEY_DATUM));
            mDatumView.setText(DbAdapter.getDateString(d));
            mDatumView.setEnabled(false);
         }
      } catch (SQLException e) {
         Log.e(TAG, e.getLocalizedMessage());
         String msg = String.format(getResources()
                                    .getString(R.string.error_sql), mRowId);
         AppMsg.makeText(this, msg, AppMsg.STYLE_ALERT).show();
      }
   }

   /**
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      saveState();
      outState.putSerializable(DbAdapter.KEY_ROWID, mRowId);
   }

   /**
    * @see android.app.Activity#onPause()
    */
   @Override
   protected void onPause() {
      super.onPause();
   }

   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   protected void onResume() {
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
   private void saveState() {
      String thema = mThemaEdit.getText().toString();
      String person = mPersonEdit.getText().toString();

      mDbHelper.updateEntry(mRowId, thema, person);
   }

   /**
    * Quits the Activity and go back to the main activity.
    */
   private void quitActivity() {
      Intent intent = new Intent(getApplicationContext(), MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
   }
}
