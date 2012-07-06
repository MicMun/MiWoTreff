/**
 * HtmlParser.java
 *
 * Copyright 2012 by Michael Munzert
 */
package com.googlemail.micmunze.miwotreff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;

/**
 * Parser f&uuml;r HTML-Zeile.
 * Parst eine HTML-Tabelle.
 *
 * @author Michael Munzert
 * @version 1.0, 06.07.2012
 */
public class HtmlParser
{
   private final String TABLE_START = "<table class='contenttable'>";
   private final String TABLE_END = "</table>";
   private final String TAG = "MiWoTreff.HtmlParser";
   
   /**
    * Erzeugt neuen HtmlParser.
    */
   public HtmlParser () {}
   
   /**
    * Liefert die Tabelle als String.
    * 
    * @param  url
    *         URL der HTML-Seite.
    * @return Tabelle mit Programm.
    */
   public String getHtmlFromUrl (String u) {
      String line = null;
      
      try {
         URL url = new URL (u);
         HttpURLConnection con = (HttpURLConnection) url.openConnection ();
         con.setUseCaches (false);
         con.setRequestMethod ("GET");
         con.connect ();
         BufferedReader in = new BufferedReader 
         (new InputStreamReader (con.getInputStream (), 
                                 Charset.forName ("iso-8859-1")));
         
         
         while ((line = in.readLine ()) != null) {
            if (line.contains (TABLE_START)) {
               break;
            }
         }
      } catch (MalformedURLException e) {
         Log.e (TAG, e.getLocalizedMessage ());
      } catch (IOException e) {
         Log.e (TAG, e.getLocalizedMessage ());
      }
      
      return line;
   }
   
   /**
    * Parst die Tabelle und liefert eine Liste der Zeilen (Maps) zur&uuml;ck.
    * @param  t
    *         Tabelle im HTML-Format.
    * @return Liste mit Maps (jede Zeile eine Map).
    */
   public ArrayList<HashMap<String, Object>> getProg (String t) {
      ArrayList<HashMap<String, Object>> prog = 
      new ArrayList<HashMap<String,Object>> (50);
      
      int start = t.indexOf (TABLE_START);
      start += TABLE_START.length ();
      int end = t.indexOf (TABLE_END, start);
      
      String s = t.substring (start, end);
      s = s.replace ("&nbsp;", " ");
      
      String[] rows = s.split ("<tr>");
      
      for (int i = 1; i < rows.length; ++i) {
         HashMap<String, Object> map = new HashMap<String, Object> ();
         String[] cols = rows[i].split ("<td>");
         String datum = cols[1].replace ("</td>", "");
         start = datum.indexOf (' ') + 1;
         datum = datum.substring (start);
         SimpleDateFormat sdf = new SimpleDateFormat ("dd.MM.yyyy");
         Date d = null;
         try {
            d = sdf.parse (datum.trim ());
         } catch (ParseException e) {
            Log.e (TAG, e.getLocalizedMessage ());
            return null;
         }
         String thema = cols[2].replace ("</td>", "");
         start = thema.indexOf ('>') + 1;
         end = thema.indexOf ('<', start);
         thema = thema.substring (start, end).trim ();
         
         String person = cols[3].replace ("</td></tr>", "").trim ();

         map.put (DbAdapter.KEY_DATUM, d);
         map.put (DbAdapter.KEY_THEMA, thema);
         map.put (DbAdapter.KEY_PERSON, person);
         prog.add (map);
      }
      
      return prog;
   }
}
