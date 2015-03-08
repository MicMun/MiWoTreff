/**
 * Copyright 2015 MicMun
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU >General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or >
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see >http://www.gnu.org/licenses/.
 */
package de.micmun.android.miwotreff.util;

/**
 * Infos of a calendar like id, name, etc.
 *
 * @author MicMun
 * @version 1.0, 25.02.15
 */
public class CalendarInfo {
   private long id;
   private String displayName;

   /**
    * A new empty CalendarInfo.
    */
   public CalendarInfo() {
      id = -1;
      displayName = "unknown";
   }

   /**
    * A new CalendarInfo with id, name, display name and owner name.
    *
    * @param id          id of the calendar.
    * @param displayName display name of the calendar.
    */
   public CalendarInfo(long id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   /**
    * Returns the id of the calendar.
    *
    * @return id of the calendar.
    */
   public long getId() {
      return id;
   }

   /**
    * Sets the id of the calendar.
    *
    * @param id id of the calendar.
    */
   public void setId(long id) {
      this.id = id;
   }

   /**
    * Returns the display name of the calendar.
    *
    * @return display name of the calendar.
    */
   public String getDisplayName() {
      return displayName;
   }

   /**
    * Sets the display name of the calendar.
    *
    * @param displayName display name of the calendar.
    */
   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }
}
