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
      // cancel before adding new alarm
      cancel(context);

      // intent for update service
      Intent serviceIntent = new Intent(context, UpdateIntentService.class);
      serviceIntent.setAction(Intent.ACTION_GET_CONTENT);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);
      // date of first execution = next wednesday 16 o'clock
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(System.currentTimeMillis());
      cal.set(Calendar.HOUR_OF_DAY, 16);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      // interval = every day
      long interval = AlarmManager.INTERVAL_DAY;

      // alarm manager, sets alarm
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), interval,
            pendingIntent);
   }

   /**
    * Cancel the alarm for updating service.
    *
    * @param context context of application.
    */
   public static void cancel(Context context) {
      // intent for update service
      Intent serviceIntent = new Intent(context, UpdateIntentService.class);
      serviceIntent.setAction(Intent.ACTION_GET_CONTENT);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);
      // alarm manager, cancel alarm
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.cancel(pendingIntent);
   }
}
