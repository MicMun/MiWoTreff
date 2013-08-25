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

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.micmun.android.miwotreff.utils.DBConstants;
import de.micmun.android.miwotreff.utils.DBDateUtility;

/**
 * An Activity to edit an entry.
 *
 * @author MicMun
 * @version 1.0, 18.01.2013
 */
public class EditActivity extends Activity {
   private static final String TAG = "EditActivity";
   private EditText mThemaEdit; // topic input
   private EditText mPersonEdit; // person input
   private EditText mDatumView; // Date
   private Long mRowId; // _id

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit);

      mThemaEdit = (EditText) findViewById(R.id.edit_thema);
      mPersonEdit = (EditText) findViewById(R.id.edit_person);
      mDatumView = (EditText) findViewById(R.id.edit_datum);

      mRowId = savedInstanceState == null ? null : (Long) savedInstanceState
            .getSerializable(DBConstants._ID);
      if (mRowId == null) {
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
            mRowId = extras.getLong(DBConstants._ID);
         }
      }
      populateFields();

      /*
       * Done & Discard
       */
      // Inflate a "Done/Discard" custom action bar view.
      final ActionBar actionBar = getActionBar();
      if (actionBar != null) {
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
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
               ActionBar.DISPLAY_SHOW_CUSTOM
                     | ActionBar.DISPLAY_SHOW_HOME
                     | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView,
               new ActionBar.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT));

      }
   }

   /**
    * Sets the fields with the values to edit or with default values.
    */
   private void populateFields() {
      // actual values
      Cursor c = getContentResolver().query(Uri.withAppendedPath(DBConstants
            .TABLE_CONTENT_URI, String.valueOf(mRowId)), null, null, null,
            null);

      try {
         if (c != null && c.moveToFirst()) {
            mThemaEdit.setText(c.getString(c.getColumnIndex
                  (DBConstants.KEY_THEMA)));
            mPersonEdit.setText(c.getString(c.getColumnIndex
                  (DBConstants.KEY_PERSON)));
            Long d = c.getLong(c.getColumnIndex
                  (DBConstants.KEY_DATUM));
            mDatumView.setText(DBDateUtility.getDateString(d));
            mDatumView.setEnabled(false);
         }
      } catch (IllegalArgumentException e) {
         Log.e(TAG, e.getLocalizedMessage());
      }
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
   }

   /**
    * Saves the data in the database (insert or update).
    */
   private void saveState() {
      String thema = mThemaEdit.getText().toString();
      String person = mPersonEdit.getText().toString();

      Uri uri = Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI,
            String.valueOf(mRowId));
      ContentValues values = new ContentValues();
      values.put(DBConstants.KEY_THEMA, thema);
      values.put(DBConstants.KEY_PERSON, person);
      values.put(DBConstants.KEY_EDIT, 1);
      getContentResolver().update(uri, values, null, null);
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
