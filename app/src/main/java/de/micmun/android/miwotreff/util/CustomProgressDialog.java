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


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.micmun.android.miwotreff.R;

/**
 * Custom progress dialog for showing circle progress bar.
 *
 * @author MicMun
 * @version 1.0, 15.03.15
 */
public class CustomProgressDialog extends Dialog {
   private boolean isIndeterminate;
   private ProgressWheel mProgressBar;

   /**
    * Creates a new CustomProgressDialog with context.
    *
    * @param ctx context of the app.
    */
   public CustomProgressDialog(Context ctx, boolean isIndeterminate) {
      super(ctx, R.style.CustomDialogStyle);
      this.isIndeterminate = isIndeterminate;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.custom_progress);

      mProgressBar = (ProgressWheel) findViewById(R.id.progress);
      mProgressBar.setBarColor(Color.BLUE);
      if (isIndeterminate)
         mProgressBar.spin();

      setCancelable(false);
   }

   /**
    * Sets the progress of the progress bar.
    *
    * @param value value of the progress.
    */
   public void setProgress(int value) {
      mProgressBar.setProgress(value / 100.0f);
   }
}
