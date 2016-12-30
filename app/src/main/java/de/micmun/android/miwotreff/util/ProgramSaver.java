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
package de.micmun.android.miwotreff.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.db.DBProvider;
import de.micmun.android.miwotreff.recyclerview.Program;

/**
 * Saving the loaded JSON data to database.
 *
 * @author MicMun
 * @version 1.1, 29.12.16
 */
public class ProgramSaver implements FutureCallback<JsonArray> {
   /**
    * The program refreshed callback.
    */
   private OnProgramRefreshListener mOnProgramRefreshListener = sDummyListener;

   private final String TAG = "MiWoTreff.ProgramSaver";
   private final Context mCtx;

   /**
    * Creates a new ProgramSaver with context.
    *
    * @param ctx context of caller.
    */
   public ProgramSaver(Context ctx) {
      mCtx = ctx;
   }

   @Override
   public void onCompleted(Exception e, JsonArray result) {
      if (e != null) {
         Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
         mOnProgramRefreshListener.onProgramRefreshed(-1, -1);
         return;
      }
      if (result == null) {
         Log.e(TAG, mCtx.getString(R.string.error_pl_fetch));
      } else {
         int countIns = 0;
         int countUpd = 0;

         // for all entries
         for (JsonElement element : result) {
            JsonObject o = element.getAsJsonObject();
            // take the values of an entry
            String dateString = o.get(DBConstants.KEY_DATUM).getAsString().trim();
            long date = DBDateUtility.getDateFromString(dateString).getTime();
            String topic = o.get(DBConstants.KEY_THEMA).getAsString().trim();
            String person = o.get(DBConstants.KEY_PERSON).getAsString().trim();

            // Prepare values for insert or update
            Program program = new Program(date, topic, person);

            // Query, if date exists
            DBProvider dbProvider = DBProvider.getInstance(mCtx);
            Program oldProgram = dbProvider.programExists(program);

            if (oldProgram == null) { // if not exists
               // Insert
               dbProvider.insertProgram(program);
               countIns++;
            } else { // exists
               program.set_id(oldProgram.get_id());

               if (!oldProgram.isEdited()) { // if not edited yet
                  // Update, if something has changed
                  if (!topic.equals(oldProgram.getTopic()) ||
                        !person.equals(oldProgram.getPerson())) {
                     dbProvider.updateProgram(program);
                     countUpd++;
                  }
               }
            }
         }
         // notify listener of inserted count
         mOnProgramRefreshListener.onProgramRefreshed(countIns, countUpd);
      }
   }

   /**
    * Registers a callback, to be triggered when the loading is finished.
    */
   public void setOnProgramRefreshedListener(OnProgramRefreshListener listener) {
      if (listener == null) {
         listener = sDummyListener;
      }

      mOnProgramRefreshListener = listener;
   }

   /**
    * A dummy no-op callback for use when there is no other listener set.
    */
   private static OnProgramRefreshListener sDummyListener = new OnProgramRefreshListener() {
      @Override
      public void onProgramRefreshed(int countInsert, int countUpdate) {
      }
   };

   /**
    * A callback interface used to listen for program refreshes.
    */
   public interface OnProgramRefreshListener {
      /**
       * Called when the program was refreshed.
       *
       * @param countInsert number of inserted entries.
       * @param countUpdate number of updated entries.
       */
      void onProgramRefreshed(int countInsert, int countUpdate);
   }
}

