package de.micmun.android.miwotreff;

import java.util.GregorianCalendar;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.utils.BackupActionProvider;
import de.micmun.android.miwotreff.utils.DbAdapter;
import de.micmun.android.miwotreff.utils.LoaderListener;
import de.micmun.android.miwotreff.utils.ProgramLoader;
import de.micmun.android.miwotreff.utils.SpecialCursorAdapter;

/**
 * Main Activity of the app.
 *
 * @author MicMun
 * @version 1.0, 13.01.2013
 */
public class MainActivity extends ListActivity implements LoaderListener{
	private static final String TAG = "MiWoTreff";
	private DbAdapter mDbHelper; // Database Helper
	
	private MenuItem btnRefresh = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDbHelper = new DbAdapter(this);
		try {
			mDbHelper.open();
		} catch (SQLException s) {
			Log.e(TAG, s.getLocalizedMessage());
			AppMsg.makeText(this, R.string.db_open_error, 
			                AppMsg.STYLE_ALERT).show();
			return;
		}
		fillData();
		registerForContextMenu(getListView());
	}

	/**
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		btnRefresh = menu.findItem(R.id.menu_refresh);
		// BackupActionProvider for backup/restore
		MenuItem bi = menu.findItem(R.id.menu_export);
		BackupActionProvider bap = new BackupActionProvider(this);
		bap.setActivity(this);
		bi.setActionProvider(bap);
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
				ProgramLoader pl = new ProgramLoader(this, btnRefresh);
				pl.addLoaderListener(this);
				pl.execute(new Void[] {});
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
            //delItem(info);
            return true;
         default:
            return super.onContextItemSelected(item);
      }
   }

	/**
	 * @see de.micmun.android.miwotreff.utils.LoaderListener#update()
	 */
	@Override
	public void update() {
		fillData();
	}

	/**
	 * Reads the data from database and fill the list.
	 */
	private void fillData() {
		Cursor entryCursor = mDbHelper.fetchAllEntries(null);
		SpecialCursorAdapter adapter = new SpecialCursorAdapter(this,entryCursor);
		setListAdapter(adapter);
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
//	private void delItem(AdapterContextMenuInfo info) {
//		if (!mDbHelper.deleteEntry(info.id)) {
//			Log.e(TAG, "Can't delete Entry with ID = " + info.id);
//			return;
//		}
//		fillData();
//	}
}
