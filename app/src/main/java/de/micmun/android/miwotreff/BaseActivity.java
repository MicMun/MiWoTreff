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
package de.micmun.android.miwotreff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Base activity for handling the toolbar.
 *
 * @author MicMun
 * @version 1.0, 14.01.2015
 */
public abstract class BaseActivity extends ActionBarActivity {

   /**
    * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(getLayoutResource());

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarId);

      if (toolbar != null) {
         setSupportActionBar(toolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(false);
         toolbar.setTitle(getResources().getString(R.string.app_name));
      }
   }

   /**
    * Returns the layout resource id for content view.
    *
    * @return layout resource id.
    */
   protected abstract int getLayoutResource();

   /**
    * Enables or disables display home button.
    *
    * @param e <code>true</code>, if back arrow should be enabled.
    */
   protected void setBackArrowEnabled(boolean e) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(e);
   }
}
