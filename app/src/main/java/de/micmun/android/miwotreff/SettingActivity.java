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
package de.micmun.android.miwotreff;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Activity for app settings.
 *
 * @author MicMun
 * @version 1.0, 09.02.16
 */
public class SettingActivity extends PreferenceActivity
      implements SharedPreferences.OnSharedPreferenceChangeListener {
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
      getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
      initSummary(getPreferenceScreen());
   }

   @Override
   public void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);

      LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
      Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
      bar.setTitle(R.string.action_setting);
      bar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
      root.addView(bar, 0); // insert at top
      bar.setNavigationOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            finish();
         }
      });
   }

   @Override
   protected void onResume() {
      super.onResume();
      // Set up a listener whenever a key changes
      getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   protected void onPause() {
      super.onPause();
      // Unregister the listener whenever a key changes
      getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      Preference pref = findPreference(key);
      updatePrefSummary(pref);
   }

   /**
    * Initialize all summaries.
    *
    * @param p root preference (group or category).
    */
   private void initSummary(Preference p) {
      if (p instanceof PreferenceCategory) {
         PreferenceCategory pGrp = (PreferenceCategory) p;
         for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
            initSummary(pGrp.getPreference(i));
         }
      } else if (p instanceof PreferenceGroup) {
         PreferenceGroup pGrp = (PreferenceGroup) p;
         for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
            initSummary(pGrp.getPreference(i));
         }
      } else {
         updatePrefSummary(p);
      }
   }

   /**
    * Updates the summary of the given preference.
    *
    * @param p preference to update.
    */
   private void updatePrefSummary(Preference p) {
      if (p instanceof ListPreference) {
         ListPreference listPref = (ListPreference) p;
         String summary = getString(R.string.files_keep_summary, listPref.getEntry());
         p.setSummary(summary);
      }
   }
}
