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

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.MainActivity;
import de.micmun.android.miwotreff.R;

/**
 * ActionProvider for the backup/restore item in actionbar.
 * 
 * @author MicMun
 * @version 1.0, 13.01.2013
 */
public class BackupActionProvider extends ActionProvider implements
		OnMenuItemClickListener {
	// Types of the action
	private static final String TAG = "MiWoTreff.BackupActionProvider";
	private static final String ENC = "UTF-8"; // Encoding

	private String backup = ""; // Title Backup
	private String restore = ""; // Title Restore

	private DbAdapter mDbHelper = null;
	private MainActivity ma = null;

	/**
	 * Creates a new BackupActionProvider with the context.
	 * 
	 * @param context
	 *           Context of the application.
	 */
	public BackupActionProvider(Context context) {
		super(context);
		mDbHelper = new DbAdapter(context);
		try {
			mDbHelper.open();
		} catch (SQLException s) {
			Log.e(TAG, s.getLocalizedMessage());
			AppMsg.makeText((MainActivity) context, R.string.db_open_error,
					AppMsg.STYLE_ALERT).show();
			mDbHelper = null;
			return;
		}
	}

	/**
	 * Sets the MainActivity of the App.
	 * 
	 * @param m
	 *           {@link MainActivity}.
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

		if (!DIR.exists() && !DIR.mkdirs()) {
			String msg = getMessage(R.string.error_mkdir, DIR.getAbsolutePath());
			AppMsg.makeText(ma, msg, AppMsg.STYLE_ALERT).show();
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
			String msg = getMessage(R.string.info_mkdir, file.toString());
			Log.d(TAG, msg);
			AppMsg.makeText(ma, msg, AppMsg.STYLE_INFO).show();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException -> " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException -> " + e.getLocalizedMessage());
			String msg = getMessage(R.string.error_write_file, file.toString());
			AppMsg.makeText(ma, msg, AppMsg.STYLE_ALERT).show();
		}
	}

	/**
	 * Restores the data from a file, the user can choose the file.
	 */
	private void restore() {
		if (mDbHelper == null)
			return;

		String[] items = DIR.list(); // List of names of backup files
		if (items == null || items.length == 0) {
			AppMsg.makeText(ma, ma.getResources().getString(R.string.no_restore),
					AppMsg.STYLE_INFO).show();
			return;
		}
		final String[] sortItems = new String[items.length];
		for (int i = items.length - 1; i >= 0; --i) {
			sortItems[items.length - (i + 1)] = items[i];
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ma);
		builder.setTitle(R.string.menu_restore);
		builder.setItems(sortItems, new DialogInterface.OnClickListener() {
			/**
			 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
			 *      int)
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
						sb.append((char) c);
					}
					JSONArray array = new JSONArray(sb.toString());
					mDbHelper.writeJSonData(array);
					if (ma != null)
						ma.update();
					AppMsg.makeText(ma, R.string.restore_success, AppMsg.STYLE_INFO)
							.show();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getLocalizedMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
					String msg = getMessage(R.string.error_read_file,
							file.toString());
					AppMsg.makeText(ma, msg, AppMsg.STYLE_ALERT).show();
				} catch (JSONException e) {
					Log.e(TAG, e.getLocalizedMessage());
					String msg = getMessage(R.string.error_parse_file,
							file.toString());
					AppMsg.makeText(ma, msg, AppMsg.STYLE_ALERT).show();
				} finally {
					try {
						isr.close();
					} catch (IOException e) {
						Log.e(TAG, e.getLocalizedMessage());
						isr = null;
					}
				}
			}
		});
		builder.show();
	}

	/**
	 * Returns the message from ressource with format argument if needed.
	 * 
	 * @param id
	 *           id of the string ressource.
	 * @param arg
	 *           argument string or <code>null</code>, if no argument required.
	 * @return message as string.
	 */
	private String getMessage(int id, String arg) {
		String str = ma.getResources().getString(id);
		String msg = "";
		if (arg != null) {
			msg = String.format(str, arg);
		} else {
			msg = str;
		}

		return msg;
	}
}
