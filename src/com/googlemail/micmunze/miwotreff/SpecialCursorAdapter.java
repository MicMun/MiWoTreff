/**
 * SpecialCursorAdapter.java
 *
 * Copyright 2012 by Michael Munzert
 */
package com.googlemail.micmunze.miwotreff;

import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Handles the view of the data for the {@link ListView}.
 *
 * @author Michael Munzert
 * @version 1.0, 11.08.2012
 */
public class SpecialCursorAdapter
extends ResourceCursorAdapter
implements Filterable
{   
   /**
    * Creates a new SpecialCursorAdapter with context and cursor.
    * 
    * @param  ctx
    *         Context.
    * @param  c
    *         Cursor.
    */
   public SpecialCursorAdapter(Context ctx, Cursor c) {
      super(ctx, R.layout.list_row, c, false);
   }
   
   /**
    * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
    */
   @Override
   public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder viewHolder = (ViewHolder) view.getTag();
      if (viewHolder == null) {
         viewHolder = new ViewHolder(view);
         view.setTag(viewHolder);
      }
      viewHolder.bind(cursor, context);
   }
   
   /**
    * Manage the text fields of a row (binding data to view).
    *
    * @author Michael Munzert
    * @version 1.0, 11.08.2012
    */
   private static class ViewHolder {
      private ColorStateList oldColors = null;
      private TextView datum;
      private TextView thema;
      private TextView person;
      
      /**
       * Creates a new ViewHolder with a view.
       * 
       * @param  view
       *         View.
       */
      public ViewHolder(View view) {
         datum = (TextView) view.findViewById(R.id.text_datum);
         thema = (TextView) view.findViewById(R.id.text_thema);
         person = (TextView) view.findViewById(R.id.text_person);
      }
      
      /**
       * Binds the data from cursor to the text fields.
       * 
       * @param  c
       *         Cursor.
       * @param  ctx
       *         Context.
       */
      public void bind(Cursor c, Context ctx) {
         // date
         long d = c.getLong(c.getColumnIndex(DbAdapter.KEY_DATUM));
         String sd = DbAdapter.getDateString(d);
         datum.setText(sd);
         
         if (isNextWednesday (sd)) {
            oldColors = datum.getTextColors();
            datum.setTextColor(Color.YELLOW);
         } else if (oldColors != null) {
            datum.setTextColor(oldColors);
         }
         
         // topic
         String t = c.getString(c.getColumnIndex(DbAdapter.KEY_THEMA));
         thema.setText(t);
         
         // person
         String p = c.getString(c.getColumnIndex(DbAdapter.KEY_PERSON));
         person.setText(" - " + p + " - ");
      }
      
      /**
       * Returns <code>true</code>, if the date is the next wednesday.
       * 
       * @param  d
       *         Date in format dd.MM.yyyy.
       * @return <code>true</code>, if the date is the next wednesday.
       */
      private boolean isNextWednesday (String d) {
         GregorianCalendar today = new GregorianCalendar();
         
         int diff = GregorianCalendar.WEDNESDAY - 
         today.get(GregorianCalendar.DAY_OF_WEEK);
         
         if (!(diff >= 0)) {
            diff += 7;
         }
         today.add(GregorianCalendar.DAY_OF_MONTH, diff);
         
         String strToday = DateFormat.format("dd.MM.yyyy", today).toString();
         
         if (strToday.equals(d)) {
            return true;
         }
         
         return false;
      }
   }
}
