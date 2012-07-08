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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Stellt einen speziellen CursorAdapter f&uuml;r die ListView dar.
 * Implementiert einen {@link CursorAdapter} f&uuml;r die {@link ListView}.
 *
 * @author Michael Munzert
 * @version 1.0, 07.07.2012
 */
public class SpecialCursorAdapter
extends ResourceCursorAdapter
{   
   /**
    * Erzeugt einen neuen SpecialCursorAdapter aus Context und Cursor.
    * 
    * @param  ctx
    *         Context.
    * @param  c
    *         Cursor.
    */
   public SpecialCursorAdapter (Context ctx, Cursor c) {
      super (ctx, R.layout.list_row, c, false);
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
    * Klasse enth&auml;lt die einzelnen Textfelder einer Zeile.
    * Managt das Binding der Daten aus der DB zu den Textfeldern.
    *
    * @author Michael Munzert
    * @version 1.0, 07.07.2012
    */
   private static class ViewHolder {
      private ColorStateList oldColors = null;
      private TextView datum;
      private TextView thema;
      private TextView person;
      
      /**
       * Erzeugt einen neuen ViewHolder mit einer View.
       * 
       * @param  view
       *         View.
       */
      public ViewHolder (View view) {
         datum = (TextView) view.findViewById (R.id.text_datum);
         thema = (TextView) view.findViewById (R.id.text_thema);
         person = (TextView) view.findViewById (R.id.text_person);
      }
      
      /**
       * Bindet die Daten aus Cursor an die Textfelder.
       * 
       * @param  c
       *         Cursor.
       * @param  ctx
       *         Context.
       */
      public void bind (Cursor c, Context ctx) {
         // Datum
         long d = c.getLong (c.getColumnIndex (DbAdapter.KEY_DATUM));
         GregorianCalendar gc = new GregorianCalendar ();
         gc.setTimeInMillis (d);
         String sd = DateFormat.format ("dd.MM.yyyy", gc).toString ();
         datum.setText (sd);
         
         if (isNextWednesday (sd)) {
            oldColors = datum.getTextColors ();
            datum.setTextColor (Color.YELLOW);
         } else if (oldColors != null) {
            datum.setTextColor (oldColors);
         }
         
         // Thema
         String t = c.getString (c.getColumnIndex (DbAdapter.KEY_THEMA));
         thema.setText (t);
         
         // Person
         String p = c.getString (c.getColumnIndex (DbAdapter.KEY_PERSON));
         person.setText (" - " + p + " - ");
      }
      
      /**
       * Liefert <code>true</code>, wenn das Datum der kommende Mittwoch ist.
       * 
       * @param  d
       *         Datum in Millisekunden.
       * @return <code>true</code>, wenn das Datum der kommende Mittwoch ist.
       */
      private boolean isNextWednesday (String d) {
         GregorianCalendar today = new GregorianCalendar ();
         int diff = GregorianCalendar.WEDNESDAY - today.get 
                                                (GregorianCalendar.DAY_OF_WEEK);
         if (!(diff >= 0)) {
            diff += 7;
         }
         today.add (GregorianCalendar.DAY_OF_MONTH, diff);
                
         String strToday = DateFormat.format ("dd.MM.yyyy", today).toString ();
         
         if (strToday.equals (d)) {
            return true;
         }
         
         return false;
      }
   }
}
