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
import android.os.Bundle;
import android.util.Log;

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
   public CustomProgressDialog(Context ctx) {
      super(ctx, R.style.CustomDialogStyle);
      isIndeterminate = true;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.custom_progress);

      mProgressBar = (ProgressWheel) findViewById(R.id.progress);
      setCancelable(false);
   }

   /**
    * Sets the indeterminate status.
    *
    * @param isIndeterminate <<code>true</code>, if the progress bar is indeterminate.
    */
   public void setIndeterminate(boolean isIndeterminate) {
      this.isIndeterminate = isIndeterminate;
   }

   /**
    * Progress wheel in indeterminate mode is spinning.
    */
   public void spin() {
      mProgressBar.spin();
   }

   /**
    * Stop spinning.
    */
   public void stop() {
      mProgressBar.stopSpinning();
   }

   /**
    * Sets the progress of the progress bar.
    *
    * @param value value of the progress.
    */
   public void setProgress(float value) {
      mProgressBar.setProgress(value);
   }
}
