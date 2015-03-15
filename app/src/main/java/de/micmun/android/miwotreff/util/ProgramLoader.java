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
package de.micmun.android.miwotreff.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import de.micmun.android.miwotreff.BaseActivity;
import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * Loads the program from the website and save it in the database.
 *
 * @author Michael Munzert
 * @version 1.0, 31.01.2015
 */
public class ProgramLoader extends AsyncTask<Void, Void, Integer> {
   private final String TAG = "MiWoTreff.ProgramLoader";
   private final Context mCtx;
   private final String mVon;
   private ArrayList<LoaderListener> listener = new ArrayList<>();
   private ConnectivityManager mConManager;
   private int counter;
   private CustomProgressDialog mCustomProgressDialog;

   public ProgramLoader(Context ctx, String von) {
      super();
      mCtx = ctx;
      mVon = von;
      mConManager = (ConnectivityManager) ctx.getSystemService(Context
            .CONNECTIVITY_SERVICE);
      mCustomProgressDialog = new CustomProgressDialog(mCtx);
      mCustomProgressDialog.setIndeterminate(true);
   }

   /**
    * Returns <code>true</code>, if you are connected to the internet.
    *
    * @return <code>true</code>, if connected to the internet.
    */
   private boolean isOnline() {
      boolean ret = false;
      NetworkInfo ni = mConManager.getActiveNetworkInfo();

      if (ni != null && ni.isConnected() && !ni.isRoaming())
         ret = true;

      return ret;
   }

   /**
    * @see android.os.AsyncTask#doInBackground(Object[])
    */
   @Override
   protected Integer doInBackground(Void... params) {
      publishProgress();

      if (!isOnline()) {
         Log.e(TAG, "No Internet connection!");
         CustomToast.makeText((BaseActivity) mCtx, mCtx.getString(R.string.error_pl_noconnect),
               CustomToast.TYPE_ERROR).show();
         return 1;
      } else {
         String result = readProgramm("http://www.mittwochstreff-muenchen.de/program/api/index.php?op=0&von=" + mVon);

         if (result == null) {
            Log.e(TAG, "Can't fetch program!");
            CustomToast.makeText((BaseActivity) mCtx, mCtx.getString(R.string.error_pl_fetch),
                  CustomToast.TYPE_ERROR).show();
            return 1;
         } else {
            JSONArray progList;
            try {
               progList = new JSONArray(result);
            } catch (JSONException e) {
               Log.e(TAG, "No data!");
               CustomToast.makeText((BaseActivity) mCtx, mCtx.getString(R.string.error_pl_parse),
                     CustomToast.TYPE_ERROR).show();
               return 1;
            }
            counter = 0;

            for (int i = 0; i < progList.length(); ++i) {
               JSONObject progPoint;
               try {
                  progPoint = progList.getJSONObject(i);
               } catch (JSONException e) {
                  Log.e(TAG, "Wrong JSON-Format!\n" + e.getLocalizedMessage());
                  CustomToast.makeText((BaseActivity) mCtx, mCtx.getString(R.string.error_pl_parse),
                        CustomToast.TYPE_ERROR).show();
                  return 1;
               }

               long date;
               String topic;
               String person;
               try {
                  date = DBDateUtility.getDateFromString((String) progPoint
                        .get(DBConstants.KEY_DATUM)).getTime();
                  topic = (String) progPoint.get(DBConstants.KEY_THEMA);
                  person = (String) progPoint.get(DBConstants.KEY_PERSON);
               } catch (JSONException e) {
                  Log.e(TAG, "Wrong JSON-Format!\n" + e.getLocalizedMessage());
                  CustomToast.makeText((BaseActivity) mCtx, mCtx.getString(R.string.error_pl_parse),
                        CustomToast.TYPE_ERROR).show();
                  return 1;
               }
               String[] selArgs = {String.valueOf(date)};

               // Prepare values for insert or update
               ContentValues values = new ContentValues();
               values.put(DBConstants.KEY_DATUM, date);
               values.put(DBConstants.KEY_THEMA, topic);
               values.put(DBConstants.KEY_PERSON, person);
               values.put(DBConstants.KEY_EDIT, 0);

               // Query, if date exists
               Uri uri = Uri.withAppendedPath(DBConstants
                     .TABLE_CONTENT_URI, DBConstants.DATE_QUERY);
               Cursor c = mCtx.getContentResolver().query(uri, null, null,
                     selArgs, null);

               if (c == null || c.getCount() <= 0) { // if not exists
                  // Insert
                  mCtx.getContentResolver()
                        .insert(DBConstants.TABLE_CONTENT_URI, values);
                  counter++;
               } else { // exists
                  c.moveToFirst();
                  int edit = c.getInt(c.getColumnIndex(DBConstants
                        .KEY_EDIT));
                  int id = c.getInt(c.getColumnIndex(DBConstants._ID));

                  if (edit == 0) { // if not edited yet
                     // Update
                     uri = Uri.withAppendedPath(DBConstants
                           .TABLE_CONTENT_URI, String.valueOf(id));
                     mCtx.getContentResolver().update(uri, values,
                           null, null);
                  }
               }
               if (c != null) {
                  c.close();
               }
            }
         }
      }

      return 0;
   }

   /**
    * Returns the programm from the website with url.
    *
    * @param u url of the api website.
    * @return program in json.
    */
   private String readProgramm(String u) {
      String line;
      StringBuilder progText = new StringBuilder();

      try {
         URL url = new URL(u);
         HttpURLConnection con = (HttpURLConnection) url.openConnection();
         con.setUseCaches(false);
         con.setRequestMethod("GET");
         con.connect();
         BufferedReader in = new BufferedReader
               (new InputStreamReader(con.getInputStream(),
                     Charset.forName("iso-8859-1")));


         while ((line = in.readLine()) != null) {
            progText.append(line);
         }
      } catch (MalformedURLException e) {
         Log.e(TAG, e.getLocalizedMessage());
         return null;
      } catch (IOException e) {
         Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
         return null;
      }

      return progText.toString();
   }

   @Override
   protected void onProgressUpdate(Void... values) {
      mCustomProgressDialog.show();
      mCustomProgressDialog.spin();
   }

   /**
    * @see android.os.AsyncTask#onPostExecute
    */
   @Override
   protected void onPostExecute(Integer result) {
      mCustomProgressDialog.stop();
      mCustomProgressDialog.cancel();

      if (result == 0) {
         notifyLoaderListener();
      }
   }

   /**
    * Adds a LoaderListener to the list of listener.
    *
    * @param l {@link LoaderListener} to add.
    */
   public void addLoaderListener(LoaderListener l) {
      listener.add(l);
   }

   /**
    * Notifies all listener.
    */
   protected void notifyLoaderListener() {
      for (LoaderListener l : listener) {
         l.update(counter);
      }
   }
}
