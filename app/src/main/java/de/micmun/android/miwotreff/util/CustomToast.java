/*
 * Copyright 2015 MicMun
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
package de.micmun.android.miwotreff.util;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.micmun.android.miwotreff.BaseActivity;
import de.micmun.android.miwotreff.R;

/**
 * Static methods for help with standard work.
 *
 * @author MicMun
 * @version 1.0, 12.02.2015
 */
public class CustomToast {
   public static final int TYPE_INFO = 0;
   public static final int TYPE_ERROR = 1;

   public static Toast makeText(BaseActivity ctx, String msg, int type) {
      // get your custom_toast.xml ayout
      LayoutInflater inflater = ctx.getLayoutInflater();

      ViewGroup vg = (ViewGroup) ctx.findViewById(R.id.custom_toast_layout_id);
      View layout = inflater.inflate(R.layout.custom_toast, vg);

      // set a message
      TextView text = (TextView) layout.findViewById(R.id.toast_msg);
      text.setText(msg);

      int color;
      switch (type) {
         case TYPE_INFO:
            color = R.color.toast_info_green;
            break;
         case TYPE_ERROR:
            color = R.color.toast_error_red;
            break;
         default: // Info as default
            color = R.color.toast_info_green;
            break;
      }
      layout.setBackgroundColor(ctx.getResources().getColor(color));

      // build Toast
      Toast toast = new Toast(ctx);
      int yOffset = ctx.getSupportActionBar().getHeight();
      toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, yOffset);
      toast.setDuration(Toast.LENGTH_LONG);
      toast.setView(layout);

      return toast;
   }
}
