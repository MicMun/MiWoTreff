package de.micmun.android.miwotreff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main Activity with the features of the app.
 *
 * @author Michael Munzert
 * @version 1.0, 11.08.2012
 */
public class MainActivity extends ListActivity {
   public static final int TYPE_BACKUP = 0;
   public static final int TYPE_RESTORE = 1;
   private static final String ENC = "UTF-8";
   private static final int ACTIVITY_EDIT = 1; // Edit Entry
   private static final String TAG = "MiWoTreff";
   private DbAdapter mDbHelper; // Database Helper
   
   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      mDbHelper = new DbAdapter(this);
      try {
         mDbHelper.open();
      } catch (SQLException s) {
         String text = getResources().getString(R.string.db_open_error);
         showError(text);
         return;
      }
      fillData();
      registerForContextMenu(getListView());
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
   
   /**
    * @see android.app.ListActivity#onDestroy()
    */
   @Override
   public void onDestroy() {
      mDbHelper.close();
      super.onDestroy();
   }
   
   /**
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }
   
   /**
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId())  {
         case R.id.menu_search:
            onSearchRequested();
            return true;
         case R.id.menu_refresh:
            importProg();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
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
      
      //      ImageView image = (ImageView) layout.findViewById(R.id.error_icon);
      TextView text = (TextView) layout.findViewById(R.id.error_msg);
      text.setText(msg);
      
      Toast toast = new Toast(getApplicationContext());
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.setDuration(Toast.LENGTH_LONG);
      toast.setView(layout);
      toast.show();
   }
   
   /**
    * Fills Data from Database in List.
    */
   private void fillData() {
      Cursor entryCursor = mDbHelper.fetchAllEntries(null);
      SpecialCursorAdapter adapter = new SpecialCursorAdapter(this,entryCursor);
      setListAdapter(adapter);
   }
   
   /**
    * Imports the program from the homepage of the "Evangelische Gemeinschaft".
    */
   private void importProg() {
      if (isOnline())
         new ImportTask().execute(new Void[] {});
      else {
         String text = getResources().getString(R.string.no_connection);
         showError(text);
      }
   }
   
   /**
    * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
    */
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);
      // Edit the entry
      Intent i = new Intent(this, EditActivity.class);
      i.putExtra(DbAdapter.KEY_ROWID, id);
      startActivityForResult(i, ACTIVITY_EDIT);
   }
   
   /**
    * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, 
                                   Intent intent) {
      super.onActivityResult(requestCode, resultCode, intent);
      if (requestCode == ACTIVITY_EDIT)
         fillData();
   }
   
   /**
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, 
                                   ContextMenu.ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.context_menu, menu);
   }
   
   /**
    * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
      
      switch (item.getItemId()) {
         case R.id.addToCal: // Add to google calendar
            add2Call(info);
            return true;
         case R.id.delItem: // Delete Item
            delItem(info);
            return true;
         default:
            return super.onContextItemSelected(item);
      }
   }
   
   /**
    * Adds the entry to the calendar.
    * 
    * @param  info
    *         Info about the entry.
    */
   private void add2Call(AdapterContextMenuInfo info) {
      Cursor c = mDbHelper.fetchEntry(info.id);
      // Date and time of the calendar entry
      long d = c.getLong(1);
      GregorianCalendar start = new GregorianCalendar();
      GregorianCalendar end = new GregorianCalendar();
      start.setTimeInMillis(d);
      start.set(GregorianCalendar.HOUR_OF_DAY, 19);
      start.set(GregorianCalendar.MINUTE, 30);
      end.setTimeInMillis(d);
      end.set(GregorianCalendar.HOUR_OF_DAY, 21);
      
      // title
      String title = getResources().getString(R.string.cal_prefix) + " " +  
      c.getString(2);
      
      // location
      String loc = getResources().getString(R.string.cal_loc);
      
      // Calendar: Insert per Intent
      Intent intent = new Intent(Intent.ACTION_INSERT)
      .setData(Events.CONTENT_URI)
      .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTimeInMillis())
      .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTimeInMillis())
      .putExtra(Events.TITLE, title)
      .putExtra(Events.EVENT_LOCATION, loc)
      .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
      startActivity(intent);
   }
   
   /**
    * Deletes an item from database and list.
    * 
    * @param  info
    *         Info about the entry.
    */
   private void delItem(AdapterContextMenuInfo info) {
      if (!mDbHelper.deleteEntry(info.id)) {
         showError("Can't delete Entry with ID = " + info.id);
         return;
      }
      fillData();
   }
   
   /**
    * Returns <code>true</code>, if you are connected to the internet.
    * @return
    */
   private boolean isOnline() {
      boolean ret = false;
      ConnectivityManager cm = (ConnectivityManager)
      getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo ni = cm.getActiveNetworkInfo();
      
      if (ni != null && ni.isConnected() && !ni.isRoaming())
         ret = true;
      
      return ret;
   }
   
   /**
    * Imports the program from Homepage asynchronous in the background.
    *
    * @author Michael Munzert
    * @version 1.0, 12.08.2012
    */
   private class ImportTask extends AsyncTask<Void, Void, Integer>
   {      
      /**
       * @see android.os.AsyncTask#doInBackground(Params[])
       */
      @Override
      protected Integer doInBackground(Void... params) {
         HtmlParser parser = new HtmlParser();
         String table = parser.getHtmlFromUrl 
         ("http://www.gemeinschaft-muenchen.de/index.php?id=7&no_cache=1");
         if (table != null) {
            ArrayList<HashMap<String, Object>> prog = parser.getProg(table);
            if (prog != null) {
               for (HashMap<String, Object> m : prog) {
                  mDbHelper.createEntry((Date)m.get(DbAdapter.KEY_DATUM), 
                                        (String)m.get(DbAdapter.KEY_THEMA), 
                                        (String)m.get(DbAdapter.KEY_PERSON));
               }
            }
         }
         return null;
      }
      
      /**
       * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
       */
      @Override
      protected void onPostExecute(Integer result) {
         fillData();
      }
   }
   
   /**
    * Backup or restore the data of the app.
    * 
    * @param  type
    *         Type backup or restore.
    */
   public void backupOrRestore(int type) {
      switch (type) {
         case TYPE_BACKUP:
            backup();
            break;
         case TYPE_RESTORE:
            restore();
            break;
         default:
            Log.e(TAG, "ERROR: Invalid type.");
            break;
      }
   }
   
   private final File DIR = new File(Environment.getExternalStorageDirectory(), 
                                     "miwotreff");
   
   /**
    * Performs the backup.
    */
   private void backup() {
      JSONArray data = mDbHelper.getJSonData();
      
      if (!DIR.exists() && !DIR.mkdirs()) {
         showError("Can't create directory '" + DIR.getAbsolutePath() + "'!");
         return;
      }
      String time = "" + new Date().getTime();
      File file = new File(DIR, "miwotreff_" + time);
      
      try {
         FileOutputStream fos = new FileOutputStream(file);
         OutputStreamWriter osw = new OutputStreamWriter(fos, ENC);
         osw.write(data.toString());
         osw.flush();
         osw.close();
         Toast.makeText(this, "Backup in File " + file.toString(), 
                        Toast.LENGTH_LONG).show();
      } catch (FileNotFoundException e) {
         showError("FileNotFoundException -> " + e.getLocalizedMessage());
      } catch (IOException e) {
         showError("IOException -> " + e.getLocalizedMessage());
      }
   }
   
   /**
    * Performs the restore.
    */
   private void restore() {
      String[] items = DIR.list(); // List of names of backup files
      final String[] sortItems = new String[items.length];
      for (int i = items.length-1;i >= 0;--i) {
         sortItems[items.length-(i+1)] = items[i];
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.label_restore);
      builder.setItems(sortItems, new DialogInterface.OnClickListener() {
         /**
          * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
          */
         @Override
         public void onClick(DialogInterface dialog, int which) {
            File file = new File(DIR, sortItems[which]);
            InputStreamReader isr = null;
            
            try {
               FileInputStream fis = new FileInputStream(file);
               isr = new InputStreamReader(fis, ENC);
               int c;
               StringBuffer sb = new StringBuffer();
               
               while ((c = isr.read()) != -1) {
                  sb.append((char)c);
               }
               JSONArray array = new JSONArray(sb.toString());
               mDbHelper.writeJSonData(array);
               fillData();
            } catch (FileNotFoundException e) {
               showError(e.getLocalizedMessage());
            } catch (IOException e) {
               showError(e.getLocalizedMessage());
            } catch (JSONException e) {
               showError(e.getLocalizedMessage());
            } finally {
               try {
                  isr.close();
               } catch (IOException e) {
                  isr = null;
               }
            }
         }
      });
      builder.show();
   }
}
