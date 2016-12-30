package de.micmun.android.miwotreff.recyclerview;

import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * One Entry of the program.
 *
 * @author MicMun
 * @version 1.0, 29.12.16
 */

public class Program {
   private static String wednesday = null;

   private long _id = -1;
   private long date;
   private String dateString;
   private String topic;
   private String person;
   private boolean edited = false;
   private boolean nextWednesDay = false;

   /**
    * Creates a new Program with id, date, topic, person and edited flag.
    *
    * @param _id    ID of the entry.
    * @param date   date in millisecondes.
    * @param topic  topic of the entry.
    * @param person person.
    * @param edited flag, if the entry was edited or not.
    */
   public Program(long _id, long date, String topic, String person, boolean edited) {
      this._id = _id;
      this.date = date;
      dateString = DBDateUtility.getDateString(date);
      this.topic = topic;
      this.person = person;
      this.edited = edited;

      if (wednesday == null) {
         wednesday = DBDateUtility.getNextWednesday();
      }

      if (wednesday.equals(dateString)) {
         nextWednesDay = true;
      }
   }

   /**
    * Creates a new Program with date, topic and person.
    *
    * @param date   date in millisecondes.
    * @param topic  topic of the entry.
    * @param person person.
    */
   public Program(long date, String topic, String person) {
      this.date = date;
      dateString = DBDateUtility.getDateString(date);
      this.topic = topic;
      this.person = person;

      if (wednesday == null) {
         wednesday = DBDateUtility.getNextWednesday();
      }

      if (wednesday.equals(dateString)) {
         nextWednesDay = true;
      }
   }

   public long get_id() {
      return _id;
   }

   public void set_id(long _id) {
      this._id = _id;
   }

   public long getDate() {
      return date;
   }

   public void setDate(long date) {
      this.date = date;
   }

   public String getTopic() {
      return topic;
   }

   public void setTopic(String topic) {
      this.topic = topic;
   }

   public String getPerson() {
      return person;
   }

   public void setPerson(String person) {
      this.person = person;
   }

   public boolean isEdited() {
      return edited;
   }

   public void setEdited(boolean edited) {
      this.edited = edited;
   }

   public String getDateString() {
      return dateString;
   }

   public boolean isNextWednesDay() {
      return nextWednesDay;
   }

   @Override
   public String toString() {
      return "Program {" + dateString + ": " + topic + " - " + person + "}";
   }
}
