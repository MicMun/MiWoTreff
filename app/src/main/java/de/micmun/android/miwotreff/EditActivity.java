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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * Activity for editing one program entry.
 *
 * @author MicMun
 * @version 1.0, 19.02.2015
 */
public class EditActivity extends BaseActivity {
   private static final String TAG = "EditActivity";
   private EditText mThemaEdit; // topic input
   private EditText mPersonEdit; // person input
   private EditText mDatumView; // Date
   private Long mRowId; // _id

   /**
    * @see de.micmun.android.miwotreff.BaseActivity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setBackArrowEnabled(true);

      // sets the title
      setTitle(R.string.edit_title);

      // sets the elements of the activity
      mThemaEdit = (EditText) findViewById(R.id.edit_thema);
      mPersonEdit = (EditText) findViewById(R.id.edit_person);
      mDatumView = (EditText) findViewById(R.id.edit_datum);

      // row id to edit
      mRowId = savedInstanceState == null ? null : (Long) savedInstanceState
            .getSerializable(DBConstants._ID);
      if (mRowId == null) {
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
            mRowId = extras.getLong(DBConstants._ID);
         }
      }
      populateFields();
   }

   /**
    * @see BaseActivity#getLayoutResource()
    */
   @Override
   protected int getLayoutResource() {
      return R.layout.activity_edit;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_edit, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      switch (id) {
         case R.id.action_done:
            saveState(); // Save the changes
            quitActivity(); // back to main activity
            return true;
         case android.R.id.home:
            quitActivity(); // back to main activity
            return true;
      }

      return super.onOptionsItemSelected(item);
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

            c.close();
         }
      } catch (IllegalArgumentException e) {
         Log.e(TAG, e.getLocalizedMessage());
      }
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

   /**
    * @see android.support.v7.app.ActionBarActivity#onPause()
    */
   @Override
   protected void onPause() {
      super.onPause();
   }

   /**
    * @see android.support.v7.app.ActionBarActivity#onResume()
    */
   @Override
   protected void onResume() {
      super.onResume();
      populateFields();
   }

   /**
    * @see android.support.v7.app.ActionBarActivity#onDestroy()
    */
   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
