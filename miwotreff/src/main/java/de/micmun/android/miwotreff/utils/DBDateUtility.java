package de.micmun.android.miwotreff.utils;

import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Beschreibung.
 *
 * @author: Michael Munzert
 * @version: 1.0, 12.08.13.
 */
public class DBDateUtility {
    private static final String TAG = "DBDateUtility";
    private static final Locale def = Locale.getDefault();

    /**
     * Returns the Date-Object from String.
     *
     * @param d Date as String (Format: dd.MM.yyyy)
     * @return Date-Object.
     */
    public static Date getDateFromString(String d) {
        Date datum = null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", def);
        try {
            datum = sdf.parse(d);
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
            datum = null;
        }

        return datum;
    }

    /**
     * Returns the Date as a String.
     *
     * @param t timestamp in milliseconds (see {@link java.util.Date#getTime()}).
     * @return String of the date.
     */
    public static String getDateString(long t) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(t);
        return DateFormat.format("dd.MM.yyyy", gc).toString();
    }
}
