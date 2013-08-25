package de.micmun.android.miwotreff.utils;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Constants for the database.
 * Provides Keys for columns, content uris, etc.
 *
 * @author: Michael Munzert
 * @version: 1.0, 12.08.13.
 */
public class DBConstants implements BaseColumns {
    /**
     * Key for column <code>datum</code> (date).
     */
    public static final String KEY_DATUM = "datum";
    /**
     * Key for column <code>thema</code> (topic).
     */
    public static final String KEY_THEMA = "thema";
    /**
     * Key for column <code>person</code> (person).
     */
    public static final String KEY_PERSON = "person";
    /**
     * Key for column <code>edit</code> (edit).
     */
    public static final String KEY_EDIT = "edit";
    /**
     * Name of the table.
     */
    public static final String TABLE_NAME = "programm";
    /**
     * Authority for the content provider.
     */
    public static final String AUTHORITY = "de.micmun.android.miwotreff.provider";
    /**
     * Base of content uris.
     */
    public static final String BASE_URI = "content://" + AUTHORITY;
    /**
     * Content uri.
     */
    public static final Uri TABLE_CONTENT_URI = Uri.parse(BASE_URI + "/" + TABLE_NAME);
}
