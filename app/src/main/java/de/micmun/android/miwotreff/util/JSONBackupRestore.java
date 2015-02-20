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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import de.micmun.android.miwotreff.MainActivity;
import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * Backup and restore data in json.
 *
 * @author Michael Munzert
 * @version 1.0, 31.01.2015
 */
public class JSONBackupRestore extends AsyncTask<Object, Void, Integer> {
   // Types to execute
   public static final int TYPE_BACKUP = 0; // Type Backup
   public static final int TYPE_RESTORE = 1; // Type Restore
   public static final int TYPE_DELETE = 2; // Type Delete
   // For Logcat and encoding for file writer
   private static final String TAG = "JSONBackupRestore";
   private static final String ENC = "UTF-8"; // Encoding
   // directory to store backup files
   private final File DIR = new File(Environment.getExternalStorageDirectory(),
         "miwotreff");
   private final MainActivity mContext;
   private int mType;
   private File mFile;
   private String mMessage = null;

   /**
    * Creates a new JSONBackupRestore object.
    *
    * @param context Activity, which shows the messages.
    * @param type    Type of Action (backup or restore)
    */
   public JSONBackupRestore(MainActivity context, int type) {
      mContext = context;
      mType = type;
   }

   /**
    * Returns the existing backup files.
    *
    * @return backup files.
    */
   public File[] getBackupFiles() {
      File[] files = null;

      if (DIR.exists()) {
         files = DIR.listFiles();
         // sort descend
         Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
               return rhs.compareTo(lhs);
            }

            @Override
            public boolean equals(Object object) {
               return (object instanceof File) && this.equals(object);
            }
         });
      }

      return files;
   }

   /**
    * Backup the data from cursor in a json file.
    */
   private int backup() {
      int rc = 0;
      // Loading data and backup
      Cursor c = mContext.getContentResolver().query(DBConstants
            .TABLE_CONTENT_URI, null, null, null, null);
      if (c != null) {
         rc = writeCursorToFile(c);
      }
      return rc;
   }

   /**
    * Restore the data from the file.
    */
   private int restore() {
      int rc = 0;
      InputStreamReader isr = null;

      try {
         // read json file
         FileInputStream fis = new FileInputStream(mFile);
         isr = new InputStreamReader(fis, ENC);
         int c;
         StringBuilder sb = new StringBuilder();

         while ((c = isr.read()) != -1) {
            sb.append((char) c);
         }
         JSONArray array = new JSONArray(sb.toString());
         int count = array.length();
         ContentValues[] values = new ContentValues[count];

         // insert data into database
         for (int i = 0; i < array.length(); ++i) {
            JSONObject o = array.getJSONObject(i);
            ContentValues v = new ContentValues();

            Date d = DBDateUtility.getDateFromString(o.getString
                  (DBConstants.KEY_DATUM));
            v.put(DBConstants.KEY_DATUM, d.getTime());
            v.put(DBConstants.KEY_THEMA, o.getString(DBConstants.KEY_THEMA));
            v.put(DBConstants.KEY_PERSON, o.getString(DBConstants.KEY_PERSON));
            v.put(DBConstants.KEY_EDIT, o.getInt(DBConstants.KEY_EDIT));
            values[i] = v;
         }
         int rows = mContext.getContentResolver().bulkInsert(DBConstants
               .TABLE_CONTENT_URI, values);
         mMessage = getMessage(R.string.restore_success, String.valueOf(rows));
      } catch (FileNotFoundException e) {
         Log.e(TAG, e.getLocalizedMessage());
         rc = 2;
      } catch (IOException e) {
         Log.e(TAG, e.getLocalizedMessage());
         mMessage = getMessage(R.string.error_read_file, null);
         rc = 2;
      } catch (JSONException e) {
         Log.e(TAG, e.getLocalizedMessage());
         mMessage = getMessage(R.string.error_parse_file, null);
         rc = 2;
      } finally {
         if (isr != null)
            try {
               isr.close();
            } catch (IOException ignored) {
            }
      }
      return rc;
   }


   /**
    * Writes the data from cursor to json file.
    *
    * @param cursor cursor with data.
    */
   private int writeCursorToFile(Cursor cursor) {
      int rc = 0;
      JSONArray dataList = new JSONArray();
      JSONObject data;

      if (cursor.getCount() <= 0) {
         Log.d(TAG, "No DATA!");
         rc = 1;
      } else {
         // creates the json array for backup to file
         cursor.moveToFirst();
         do {
            String d = DBDateUtility.getDateString(cursor.getLong(cursor
                  .getColumnIndex(DBConstants.KEY_DATUM)));
            String t = cursor.getString(cursor.getColumnIndex(DBConstants
                  .KEY_THEMA));
            String p = cursor.getString(cursor.getColumnIndex(DBConstants
                  .KEY_PERSON));
            int ed = cursor.getInt(cursor.getColumnIndex(DBConstants
                  .KEY_EDIT));
            data = new JSONObject();

            try {
               data.put(DBConstants.KEY_DATUM, d);
               data.put(DBConstants.KEY_THEMA, t);
               data.put(DBConstants.KEY_PERSON, p);
               data.put(DBConstants.KEY_EDIT, ed);
               dataList.put(data);
            } catch (JSONException e) {
               mMessage = getMessage(R.string.error_write_file, null);
               rc = 1;
               break;
            }
         } while (cursor.moveToNext());
         cursor.close();

         if (rc == 0) {
            // check if directory exists and create if not
            if (!DIR.exists() && !DIR.mkdirs()) {
               mMessage = getMessage(R.string.error_mkdir,
                     DIR.getAbsolutePath());
               rc = 1;
            }
         }

         if (rc == 0) {
            if (dataList.length() <= 0)
               rc = 1;
         }

         if (rc == 0) {
            // write the file miwotreff_<time in milliseconds>
            String time = "" + new Date().getTime();
            File file = new File(DIR, "miwotreff_" + time);

            try {
               FileOutputStream fos = new FileOutputStream(file);
               OutputStreamWriter osw = new OutputStreamWriter(fos, ENC);
               osw.write(dataList.toString());
               osw.flush();
               osw.close();
               mMessage = getMessage(R.string.info_mkdir, file.toString());
            } catch (FileNotFoundException e) {
               Log.e(TAG, "FileNotFoundException -> " + e.getLocalizedMessage
                     ());
               rc = 1;
            } catch (IOException e) {
               Log.e(TAG, "IOException -> " + e.getLocalizedMessage());
               mMessage = getMessage(R.string.error_write_file, null);
               rc = 1;
            }
         }
      }

      return rc;
   }

   /**
    * Returns the message from ressource with format argument if needed.
    *
    * @param id  id of the string ressource.
    * @param arg argument string or <code>null</code>, if no argument required.
    * @return message as string.
    */
   private String getMessage(int id, String arg) {
      String str = mContext.getResources().getString(id);
      String msg;
      if (arg != null) {
         msg = String.format(str, arg);
      } else {
         msg = str;
      }

      return msg;
   }

   /**
    * @see android.os.AsyncTask#doInBackground(Object[])
    */
   @Override
   protected Integer doInBackground(Object... params) {
      int rc = 0;

      switch (mType) {
         case TYPE_BACKUP: // save data in backup file
            rc = backup();
            break;
         case TYPE_RESTORE: // restore backup
            mFile = (File) params[0];
            if (mFile == null) {
               mMessage = mContext.getResources().getString(R.string
                     .no_file_selected);
               rc = 2;
            } else {
               rc = restore();
            }
            break;
         case TYPE_DELETE: // delete old backup files
            File[] delFiles = (File[]) params;
            int count = 0;
            for (File f : delFiles) {
               if (f.delete()) {
                  count++;
               } else {
                  break;
               }
            }
            if (count < delFiles.length) {
               mMessage = getMessage(R.string.error_delete, null);
               rc = 3;
            } else {
               mMessage = getMessage(R.string.count_del, String.valueOf(count));
            }
            break;
         default: // unknown type
            Log.e(TAG, "Unknown action type");
            rc = 4;
            break;
      }

      return rc;
   }

   /**
    * @see android.os.AsyncTask#onPostExecute(Object)
    */
   @Override
   protected void onPostExecute(Integer result) {
      if (mMessage == null)
         return;

      if (result == 0) {
         // Show success message
         CustomToast.makeText(mContext, mMessage, CustomToast.TYPE_INFO).show();
      } else if (result == 1) {
         CustomToast.makeText(mContext, mMessage, CustomToast.TYPE_ERROR).show();
      }
   }
}
