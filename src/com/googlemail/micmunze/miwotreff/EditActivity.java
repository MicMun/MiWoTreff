/**
 * EditActivity.java
 *
 * Copyright 2012 by Michael Munzert
 */
package com.googlemail.micmunze.miwotreff;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity f&uuml;r Erstellen oder Bearbeiten eines Programmpunktes.
 * Besteht aus einem nichteditierbaren Datumsfeld und den Feldern Thema und 
 * Person.
 *
 * @author Michael Munzert
 * @version 1.0, 14.07.2012
 */
public class EditActivity
extends Activity
{
   private static final String TAG = "EditActivity";
   
   private EditText mThemaEdit; // Thema eingeben
   private EditText mPersonEdit; // Person eingeben
   private Long mRowId; // _id
   private EditText mDatumView; // Datum
   private DbAdapter mDbHelper; // Datenbankzugriff
   
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
      populateFields ();
   }
   
   /**
    * Setzt die Felder mit den bisherigen Werten, falls vorhanden.
    */
   @SuppressWarnings ("deprecation")
   private void populateFields ()
   {
      if (mRowId != null) {
         try {
            Cursor note = mDbHelper.fetchEntry (mRowId);
            startManagingCursor (note);
            
            if (note != null) {
               mThemaEdit.setText (note.getString (note.getColumnIndexOrThrow 
                                                   (DbAdapter.KEY_THEMA)));
               mPersonEdit.setText (note.getString (note.getColumnIndexOrThrow 
                                                    (DbAdapter.KEY_PERSON)));
               Long d = note.getLong (note.getColumnIndexOrThrow 
                                      (DbAdapter.KEY_DATUM));
               mDatumView.setText (DbAdapter.getDateString (d));
               mDatumView.setEnabled (false);
            }
         } catch (SQLException e) {
            Log.e (TAG, e.getLocalizedMessage ());
         }
      } else {
         // Default-Werte
         String d = DbAdapter.getDateString (new Date().getTime ());
         String t = "Noch offen";
         String p = "Noch offen";
         mDatumView.setText (d);
         mThemaEdit.setText (t);
         mPersonEdit.setText (p);
      }
   }
   
   /**
    * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
    */
   @Override
   protected void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState (outState);
      saveState();
      outState.putSerializable (DbAdapter.KEY_ROWID, mRowId);
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
      switch (item.getItemId ()) {
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
            return super.onOptionsItemSelected (item);
      }
   }
   
   /**
    * @see android.app.Activity#onPause()
    */
   @Override
   protected void onPause()
   {
      super.onPause ();
   }
   
   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   protected void onResume()
   {
      super.onResume ();
      populateFields ();
   }
   
   @Override
   protected void onDestroy () {
      super.onDestroy ();
      mDbHelper.close ();
   }
   
   /**
    * Speichert den Datensatz in der Datenbank.
    */
   private void saveState()
   {
      String thema = mThemaEdit.getText().toString();
      String person = mPersonEdit.getText().toString();
      String datum = mDatumView.getText ().toString ();
      
      if (mRowId == null) {
         long id = mDbHelper.createEntry (DbAdapter.getDateFromString (datum), 
                                          thema, person);
         if (id > 0) {
            mRowId = id;
         } else {
            String msg = "Entry already exists";
            Toast.makeText (this, msg, Toast.LENGTH_LONG).show ();
         }
      } else {
         mDbHelper.updateEntry (mRowId, thema, person);
      }
   }
}
