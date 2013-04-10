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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.SQLException;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;

import com.devspark.appmsg.AppMsg;

import de.micmun.android.miwotreff.MainActivity;
import de.micmun.android.miwotreff.R;

/**
 * Loads the program from the website and save it in the database.
 * 
 * @author MicMun
 * @version 1.0, 13.01.2013
 * 
 */
public class ProgramLoader extends AsyncTask<Void, Void, Integer> {
	private final String TAG = "MiWoTreff.ProgramLoader";
	private ArrayList<LoaderListener> listener = new ArrayList<LoaderListener>();
	private Context mCtx;
	private DbAdapter mDbHelper = null;
	private MenuItem btnRefresh = null;
	Drawable btnRefresStaticDrawable = null;

	public ProgramLoader(Context ctx, MenuItem mi) {
		super();
		mCtx = ctx;
		btnRefresh = mi;
		btnRefresStaticDrawable = btnRefresh.getIcon();
		mDbHelper = new DbAdapter(mCtx);
		try {
			mDbHelper.open();
		} catch (SQLException s) {
			Log.e(TAG, s.getLocalizedMessage());
			AppMsg.makeText((MainActivity) ctx, R.string.db_open_error,
					AppMsg.STYLE_ALERT).show();
			return;
		}
	}

	/**
	 * Returns <code>true</code>, if you are connected to the internet.
	 * 
	 * @return <code>true</code>, if connected to the internet.
	 */
	private boolean isOnline() {
		boolean ret = false;
		ConnectivityManager cm = (ConnectivityManager) mCtx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni != null && ni.isConnected() && !ni.isRoaming())
			ret = true;

		return ret;
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(Void... params) {
		publishProgress();

		if (!isOnline()) {
			Log.e(TAG, "No Internet connection!");
			AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_noconnect,
					AppMsg.STYLE_ALERT).show();
			return 1;
		} else {
			HtmlParser parser = new HtmlParser();
			String table = parser
					.getHtmlFromUrl("http://www.gemeinschaft-muenchen.de/index.php?id=7&no_cache=1");
			if (table == null) {
				Log.e(TAG, "Can't fetch program!");
				AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_fetch,
						AppMsg.STYLE_ALERT).show();
				return 1;
			} else {
				ArrayList<HashMap<String, Object>> prog = parser.getProg(table);
				if (prog == null) {
					Log.e(TAG, "No data!");
					AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_nodata,
							AppMsg.STYLE_ALERT).show();
					return 1;
				} else {
					for (HashMap<String, Object> m : prog) {
						mDbHelper.createEntry((Date) m.get(DbAdapter.KEY_DATUM),
								(String) m.get(DbAdapter.KEY_THEMA),
								(String) m.get(DbAdapter.KEY_PERSON));
					}
				}
			}
		}
		return 0;
	}

	/**
	 * @see AsyncTask#onProgressUpdate(Progress... progress)
	 */
	@Override
	protected void onProgressUpdate(Void... progress) {
		btnRefresh.setIcon((mCtx.getResources()
				.getDrawable(R.drawable.ic_action_refresh_anim)));
		btnRefresh.setEnabled(false);
		AnimationDrawable frameAnimation = (AnimationDrawable) btnRefresh
				.getIcon();
		frameAnimation.start();
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute
	 */
	@Override
	protected void onPostExecute(Integer result) {
		btnRefresh.setIcon(btnRefresStaticDrawable);
		btnRefresh.setEnabled(true);

		if (result == 0) {
			notifyLoaderListener();
		}
		mDbHelper.close();
	}

	/**
	 * Adds a LoaderListener to the list of listener.
	 * 
	 * @param l
	 *           {@link LoaderListener} to add.
	 */
	public void addLoaderListener(LoaderListener l) {
		listener.add(l);
	}

	/**
	 * Removes a LoaderListener from the list of listener.
	 * 
	 * @param l
	 *           {@link LoaderListener} to remove.
	 */
	public void removeLoaderListener(LoaderListener l) {
		listener.remove(l);
	}

	/**
	 * Notifies all listener.
	 */
	protected void notifyLoaderListener() {
		for (LoaderListener l : listener) {
			l.update();
		}
	}
}
