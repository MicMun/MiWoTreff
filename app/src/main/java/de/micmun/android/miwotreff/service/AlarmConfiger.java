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
package de.micmun.android.miwotreff.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.micmun.android.miwotreff.db.DBDateUtility;

/**
 * Helper class to set alarm for update service.
 *
 * @author MicMun
 * @version 1.0, 08.02.16
 */
public class AlarmConfiger {
   /**
    * Sets the alarm for update service with context.
    *
    * @param context context of application or service.
    */
   public static void setAlarmService(Context context) {
      // intent for update service
      Intent serviceIntent = new Intent(context, UpdateIntentService.class);
      serviceIntent.setAction(Intent.ACTION_GET_CONTENT);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
      // date of first execution = next wednesday 13 o'clock
      Calendar cal = DBDateUtility.getNextWednesday();
      cal.set(GregorianCalendar.HOUR_OF_DAY, 13);
      cal.set(GregorianCalendar.MINUTE, 0);
      cal.set(GregorianCalendar.SECOND, 0);
      // interval = every week
      long interval = AlarmManager.INTERVAL_DAY * 7;

      // alarm manager, sets alarm
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), interval,
            pendingIntent);
   }
}
