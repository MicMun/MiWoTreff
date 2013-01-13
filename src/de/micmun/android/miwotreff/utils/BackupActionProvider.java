/**
 * BackupActionProvider.java
 *
 * Copyright 2012 by MicMun
 */
package de.micmun.android.miwotreff.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Environment;
import android.util.Log;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;
import de.micmun.android.miwotreff.MainActivity;
import de.micmun.android.miwotreff.R;

/**
 * ActionProvider for the backup/restore item in actionbar.
 *
 * @author MicMun
 * @version 1.0, 13.01.2013
 */
public class BackupActionProvider
extends ActionProvider
implements OnMenuItemClickListener
{
	// Types of the action
	private static final String TAG = "miwotreff.BackupActionProvider";
	private static final String ENC = "UTF-8"; // Encoding

	private String backup = ""; // Title Backup
	private String restore = ""; // Title Restore

	private DbAdapter mDbHelper = null;
	private MainActivity ma = null;

	/**
	 * Creates a new BackupActionProvider with the context.
	 * 
	 * @param  context
	 *         Context of the application.
	 */
	public BackupActionProvider(Context context) {
		super(context);
		mDbHelper = new DbAdapter(context);
		try {
			mDbHelper.open();
		} catch (SQLException s) {
			String text = context.getResources().getString(R.string.db_open_error);
			Log.e(TAG, text);
			mDbHelper = null;
			return;
		}
	}

	/**
	 * Sets the MainActivity of the App.
	 * 
	 * @param  m
	 *         {@link MainActivity}.
	 */
	public void setActivity(MainActivity m) {
		ma = m;
	}

	/**
	 * @see android.view.ActionProvider#onCreateActionView()
	 */
	@Override
	public View onCreateActionView() {
		return null;
	}

	/**
	 * @see android.view.ActionProvider#onPerformDefaultAction()
	 */
	@Override
	public boolean onPerformDefaultAction() {
		return super.onPerformDefaultAction();      
	}

	/**
	 * @see android.view.ActionProvider#hasSubMenu()
	 */
	@Override
	public boolean hasSubMenu() {
		return true;
	}

	/**
	 * @see android.view.ActionProvider#onPrepareSubMenu(android.view.SubMenu)
	 */
	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		subMenu.clear();
		Log.d("miwotreff.BackupActionProvider", "onPrepareSubmenu");
		MenuItem item1 = subMenu.add(R.string.menu_backup);
		item1.setOnMenuItemClickListener(this);
		backup = item1.getTitle().toString();
		MenuItem item2 = subMenu.add(R.string.menu_restore);
		item2.setOnMenuItemClickListener(this);
		restore = item2.getTitle().toString();
	}

	/**
	 * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick(android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item.getTitle().toString().equals(backup))
			backup();
		else if (item.getTitle().toString().equals(restore))
			restore();
		
		return true;
	}

	private final File DIR = new File(Environment.getExternalStorageDirectory(), 
				"miwotreff");

	/**
	 * Creates a backup of the data.
	 */
	private void backup() {
		if (mDbHelper == null)
			return;
		
		JSONArray data = mDbHelper.getJSonData();
		mDbHelper.close();

		if (!DIR.exists() && !DIR.mkdirs()) {
			Log.e(TAG, "Can't create directory '" + DIR.getAbsolutePath() + "'!");
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
			Toast.makeText(ma, "Backup in File " + file.toString(), 
			               Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException -> " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException -> " + e.getLocalizedMessage());
		}
	}

	/**
	 * Restores the data from a file, the user can choose the file.
	 */
	private void restore() {
		if (mDbHelper == null)
			return;
		
		String[] items = DIR.list(); // List of names of backup files
		final String[] sortItems = new String[items.length];
		for (int i = items.length-1;i >= 0;--i) {
			sortItems[items.length-(i+1)] = items[i];
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ma);
		builder.setTitle(R.string.menu_restore);
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
					if (ma != null)
						ma.update();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getLocalizedMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
				} catch (JSONException e) {
					Log.e(TAG, e.getLocalizedMessage());
				} finally {
					try {
						isr.close();
					} catch (IOException e) {
						isr = null;
					}
					mDbHelper.close();
				}
			}
		});
		builder.show();
	}
}
