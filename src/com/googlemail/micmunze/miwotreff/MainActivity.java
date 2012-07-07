package com.googlemail.micmunze.miwotreff;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends ListActivity {
   
   private DbAdapter mDbHelper; // Database Helper
   
   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      StrictMode.ThreadPolicy policy = new StrictMode.
      ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy); 
      
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      mDbHelper = new DbAdapter (this);
      try {
         mDbHelper.open ();
      } catch (SQLException s) {
         Toast.makeText (this, R.string.db_open_error, 
                         Toast.LENGTH_SHORT).show ();
         return;
      }
      fillData ();
   }
   
   /**
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
   }
   
   /**
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId ())  {
         case R.id.menu_search:
            
            return true;
         case R.id.menu_import:
            importProg ();
            return true;
         case R.id.menu_add:
            
            return true;
         default:
            return super.onOptionsItemSelected (item);
      }
   }
   
   /**
    * Fills Data from Database in List.
    */
   @SuppressWarnings ("deprecation")
   private void fillData() {
      Cursor entryCursor = mDbHelper.fetchAllEntries ();
      startManagingCursor (entryCursor);
      
      SpecialCursorAdapter adapter = new SpecialCursorAdapter 
      (this, entryCursor);
      setListAdapter (adapter);
   }
   
   /**
    * Importiert das Programm von der Homepage der Evangelischen Gemeinschaft.
    */
   private void importProg () {
      HtmlParser parser = new HtmlParser();
      String table = parser.getHtmlFromUrl 
      ("http://www.gemeinschaft-muenchen.de/index.php?id=7&no_cache=1");
      ArrayList<HashMap<String, Object>> prog = parser.getProg (table);
      if (prog != null) {
         for (HashMap<String, Object> m : prog) {
            mDbHelper.createEntry ((Date)m.get (DbAdapter.KEY_DATUM), 
                                   (String)m.get (DbAdapter.KEY_THEMA), 
                                   (String)m.get (DbAdapter.KEY_PERSON));
         }
      }
      fillData ();
   }
   
   //   @Override
   //   protected void onListItemClick(ListView l, View v, int position, long id) {
   //       super.onListItemClick(l, v, position, id);
   //       Intent i = new Intent(this, NoteEdit.class);
   //       i.putExtra(DbAdapter.KEY_ROWID, id);
   //       startActivityForResult(i, ACTIVITY_EDIT);
   //   }
   //
   //   @Override
   //   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
   //       super.onActivityResult(requestCode, resultCode, intent);
   //       fillData();
   //   }
}
