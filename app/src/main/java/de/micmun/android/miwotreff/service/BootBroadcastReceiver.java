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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Brodcast receiver for boot complete event.
 *
 * @author MicMun
 * @version 1.0, 08.02.16
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (action.equals("android.intent.action.BOOT_COMPLETED") ||
            action.equals("android.intent.action.QUICKBOOT_POWERON")) {
         // set alarm for update service
         AlarmConfiger.setAlarmService(context);
      }
   }
}
