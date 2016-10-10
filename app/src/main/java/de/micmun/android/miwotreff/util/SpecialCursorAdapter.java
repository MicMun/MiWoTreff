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

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.util.Calendar;

import de.micmun.android.miwotreff.R;
import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * Handles the view of the data for the {@link ListView}.
 *
 * @author Michael Munzert
 * @version 1.1, 31.08.2016
 */
public class SpecialCursorAdapter
      extends ResourceCursorAdapter
      implements Filterable {
   private Context mCtx;
   private int mNextWdPos = -1;
   private String mNextWednesday;

   /**
    * Creates a new SpecialCursorAdapter with context and cursor.
    *
    * @param ctx Context.
    * @param c   Cursor.
    */
   public SpecialCursorAdapter(Context ctx, Cursor c) {
      super(ctx, R.layout.list_row, c, false);

      mCtx = ctx;

      Calendar today = DBDateUtility.getNextWednesday();
      mNextWednesday = DateFormat.format("dd.MM.yyyy", today).toString();
   }

   /**
    * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context,
    * android.database.Cursor)
    */
   @Override
   public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder viewHolder = (ViewHolder) view.getTag();
      if (viewHolder == null) {
         viewHolder = new ViewHolder(view, context, mNextWednesday);
         view.setTag(viewHolder);
      }
      viewHolder.bind(cursor);

      if (mNextWdPos == -1 && viewHolder.isNextWednesday()) {
         mNextWdPos = cursor.getPosition();
         if (mNextWdPos + 2 < cursor.getCount()) {
            mNextWdPos += 2;
         } else if (mNextWdPos + 1 < cursor.getCount()) {
            mNextWdPos++;
         }
      }
   }

   /**
    * Returns the position of the entry below the next wednesday.
    *
    * @return the position of the entry below the next wednesday.
    */
   public int getmNextWdPos() {
      Cursor c = mCtx.getContentResolver().query(Uri.withAppendedPath(DBConstants.TABLE_CONTENT_URI,
            DBConstants.NUMBER_AFTER_DATE_QUERY), null, null, new String[]{mNextWednesday}, null);
      if (c != null) {
         c.moveToNext();
         mNextWdPos = c.getInt(0) + 1;
         Log.d(getClass().getSimpleName(), "mNextWdPos = " + mNextWdPos);
         c.close();
      }
      return mNextWdPos;
   }

   /**
    * Manage the text fields of a row (binding data to view).
    *
    * @author Michael Munzert, Maximilian Salomon
    * @version 1.0, 11.12.2012
    */
   private static class ViewHolder {

      private Context ctx;
      private ColorStateList normalTextColor = null;
      private TextView datum;
      private TextView thema;
      private TextView person;
      private RelativeLayout background;
      private String nextWednesday = null;

      private boolean isNextWednesday = false;

      /**
       * Creates a new ViewHolder with a view.
       *
       * @param view View.
       */
      public ViewHolder(View view, Context context, String nextWednesday) {
         ctx = context;
         datum = (TextView) view.findViewById(R.id.text_datum);
         thema = (TextView) view.findViewById(R.id.text_thema);
         person = (TextView) view.findViewById(R.id.text_person);
         background = (RelativeLayout) view.findViewById(R.id.row_background);

         normalTextColor = datum.getTextColors();

         this.nextWednesday = nextWednesday;
      }

      /**
       * Binds the data from cursor to the text fields.
       *
       * @param c Cursor.
       */
      @SuppressWarnings("deprecation")
      public void bind(Cursor c) {
         if (c != null) {
            // date
            long d = c.getLong(c.getColumnIndex(DBConstants.KEY_DATUM));
            String sd = DBDateUtility.getDateString(d);
            datum.setText(sd);

            if (nextWednesday != null && nextWednesday.equals(sd)) {
               background.setBackgroundDrawable(
                     ctx.getResources().getDrawable(R.drawable.background_indicator_green));
               isNextWednesday = true;
            } else if (normalTextColor != null) {
               background.setBackgroundDrawable(ctx.getResources()
                     .getDrawable(R.drawable.background_indicator_transparent));
               isNextWednesday = false;
            } else {
               isNextWednesday = false;
            }

            // topic
            String t = c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
            thema.setText(t);

            // person
            String p = c.getString(c.getColumnIndex(DBConstants.KEY_PERSON));
            person.setText(p);
         }
      }

      /**
       * Returns <code>true</code>, if the view contains the next wednesday.
       *
       * @return <code>true</code>, if the view contains the next wednesday.
       */
      public boolean isNextWednesday() {
         return isNextWednesday;
      }
   }

}
