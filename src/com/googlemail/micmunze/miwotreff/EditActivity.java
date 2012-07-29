/**
 * EditActivity.java
 *
 * Copyright 2012 by Michael Munzert
 */
package com.googlemail.micmunze.miwotreff;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
   private static boolean SAVE = true;
   
   private EditText mThemaEdit; // Thema eingeben
   private EditText mPersonEdit; // Person eingeben
   private Long mRowId; // _id
   private EditText mDatumView; // Datum
   private DbAdapter mDbHelper; // Datenbankzugriff
   private Button mConfirm; //Confirm
   private Button mAbort; // Abort
   
   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate (Bundle savedInstanceState) {
      super.onCreate (savedInstanceState);
      mDbHelper = new DbAdapter (this);
      mDbHelper.open ();
      setContentView (R.layout.activity_edit);
      setTitle (R.string.edit_title);
      
      mThemaEdit = (EditText)findViewById (R.id.edit_thema);
      mPersonEdit = (EditText)findViewById (R.id.edit_person);
      mDatumView = (EditText)findViewById (R.id.edit_datum);
      mConfirm = (Button)findViewById (R.id.confirm);
      mAbort = (Button)findViewById (R.id.abort);
      
      mRowId = savedInstanceState == null ? null : 
         (Long)savedInstanceState.getSerializable (DbAdapter.KEY_ROWID);
      if (mRowId == null) {
         Bundle extras = getIntent ().getExtras ();
         if (extras != null) {
            mRowId = extras.getLong (DbAdapter.KEY_ROWID);
         }
      }
      populateFields ();
      
      mConfirm.setOnClickListener (new View.OnClickListener() {
         /**
          * @see android.view.View.OnClickListener#onClick(android.view.View)
          */
         @Override
         public void onClick (View v) {
            setResult (RESULT_OK);
            finish ();
         }
      });
      
      mAbort.setOnClickListener (new View.OnClickListener() {
         /**
          * @see android.view.View.OnClickListener#onClick(android.view.View)
          */
         @Override
         public void onClick (View v) {
            SAVE = false;
            setResult (RESULT_CANCELED);
            finish ();
         }
      });
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
    * @see android.app.Activity#onPause()
    */
   @Override
   protected void onPause()
   {
      super.onPause ();
      saveState();
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
      if (SAVE) {
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
               Toast.makeText (this, msg, Toast.LENGTH_SHORT).show ();
            }
         } else {
            mDbHelper.updateEntry (mRowId, thema, person);
         }
      }
   }
}
