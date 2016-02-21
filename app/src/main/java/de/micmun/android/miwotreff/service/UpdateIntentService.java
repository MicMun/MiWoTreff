/*
 * Copyright 2016 MicMun
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
package de.micmun.android.miwotreff.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import de.micmun.android.miwotreff.MainActivity;
import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.util.ProgramSaver;

/**
 * Service to look for updates on miwotreff program website.
 *
 * @author MicMun
 * @version 1.0, 03.02.16
 */
public class UpdateIntentService extends IntentService
      implements ProgramSaver.OnProgramRefreshListener {
   public final String TAG = "UpdateIntentService";
   private final DateFormat myDateFormat = new SimpleDateFormat("dd.MM.y", Locale.GERMANY);
   private Date dateLastServerUpdate;

   /**
    * Creates a new UpdateIntentService.
    */
   public UpdateIntentService() {
      this("UpdateIntentService");
   }

   /**
    * Creates an IntentService.  Invoked by your subclass's constructor.
    *
    * @param name Used to name the worker thread, important only for debugging.
    */
   public UpdateIntentService(String name) {
      super(name);
   }

   @Override
   protected void onHandleIntent(Intent intent) {
      // Check intent action
      if (Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
         // check if internet connection is avalaible
         if (!isOnline()) {
            Log.e(TAG, "No internet connection!");
            return;
         }

         try {
            Date dateLastUpdate = getLastLocalUpdate();
            dateLastServerUpdate = getLastServerUpdate();

            // if update is available (server time is newer or equal last update)
            if (dateLastServerUpdate.getTime() > dateLastUpdate.getTime()) {
               // syncProgram loads the program from server to database
               syncProgram();
            }
         } catch (InterruptedException | ExecutionException | ParseException e) {
            Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
         }
      }
   }

   /**
    * Returns <code>true</code>, if you are connected to the internet.
    *
    * @return <code>true</code>, if connected to the internet.
    */
   private boolean isOnline() {
      boolean ret = false;
      ConnectivityManager mConManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo ni = mConManager.getActiveNetworkInfo();

      if (ni != null && ni.isConnected() && !ni.isRoaming())
         ret = true;

      return ret;
   }

   /**
    * Loads program.
    */
   private void syncProgram() {
      // Query, if date exists
      Uri uri = Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI, DBConstants.LAST_DATE_QUERY);
      Cursor c = getContentResolver().query(uri, null, null, null, null);
      if (c != null) {
         // get last program entry
         c.moveToNext();
         String mVon = DBDateUtility.getDateString(c.getLong(1));
         c.close();

         String url = "http://www.mittwochstreff-muenchen.de/program/api/index.php?op=0&von=" +
               mVon;
         ProgramSaver ps = new ProgramSaver(this);
         ps.setOnProgramRefreshedListener(this);

         // load program from url
         try {
            JsonArray program = Ion.with(this).load(url).asJsonArray().get();
            ps.onCompleted(null, program);
         } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
            ps.onCompleted(e, null);
         }

      }
   }

   /**
    * Returns the date of last server update.
    *
    * @return last server update.
    * @throws ParseException       error while parsing json.
    * @throws ExecutionException   error while executing task.
    * @throws InterruptedException error cause of interruption.
    */
   private Date getLastServerUpdate() throws ParseException, ExecutionException, InterruptedException {
      // Fetch last update from server
      JsonObject json = Ion.with(this)
            .load("http://mittwochstreff-muenchen.de/program/api/index.php?op=1")
            .asJsonObject().get();
      String lastServerUpdate = json.get(DBConstants.KEY_DATUM).getAsString();

      return myDateFormat.parse(lastServerUpdate);
   }

   /**
    * Returns the date of last local update.
    *
    * @return last local update.
    * @throws ParseException if an error occurred by parsing the string to date.
    */
   private Date getLastLocalUpdate() throws ParseException {
      // Fetch the last update from database
      Uri uri = Uri.withAppendedPath(DBConstants.SETTING_CONTENT_URI, DBConstants.KEY_QUERY);
      Cursor c2 = getContentResolver().query(uri, null, null,
            new String[]{DBConstants.SETTING_KEY_LAST_UPDATE}, null);
      String lastUpdate;
      if (c2 == null) { // no setting found
         lastUpdate = DBConstants.SETTING_VALUE_LAST_UPDATE;
      } else {
         c2.moveToNext();
         lastUpdate = c2.getString(0);
         c2.close();
      }

      return myDateFormat.parse(lastUpdate);
   }

   @Override
   public void onProgramRefreshed(int count) {
      // last date to settings
      ContentValues cv = new ContentValues();
      cv.put(DBConstants.KEY_VALUE,
            DBDateUtility.getDateString(dateLastServerUpdate.getTime()));
      getContentResolver().update(
            Uri.withAppendedPath(DBConstants.SETTING_CONTENT_URI, DBConstants.KEY_QUERY), cv, null,
            new String[]{DBConstants.SETTING_KEY_LAST_UPDATE});

      // notification for user to start the app, only when count > 0
      if (count > 0) {
         String title = getString(R.string.app_name);
         String text = String.format(getString(R.string.load_success), count);
         NotificationCompat.Builder notifyBuilder =
               new NotificationCompat.Builder(this);
         notifyBuilder.setSmallIcon(R.drawable.ic_notify);
         notifyBuilder.setContentTitle(title);
         notifyBuilder.setContentText(text);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notifyBuilder.setCategory(Notification.CATEGORY_SOCIAL);
         }
         notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
         // result intent with date of last server update
         Intent resultIntent = new Intent(this, MainActivity.class);

         // stack for navigation
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
         stackBuilder.addParentStack(MainActivity.class);
         stackBuilder.addNextIntent(resultIntent);
         PendingIntent resultPendingIntent =
               stackBuilder.getPendingIntent(
                     0,
                     PendingIntent.FLAG_UPDATE_CURRENT
               );
         notifyBuilder.setContentIntent(resultPendingIntent);
         NotificationManager mNotificationManager =
               (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         // mId allows you to update the notification later on.
         int mId = 1;
         Notification notification = notifyBuilder.build();
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         mNotificationManager.notify(mId, notification);
      }
   }
}
