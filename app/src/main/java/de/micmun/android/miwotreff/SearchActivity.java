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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;
import java.util.List;

import de.micmun.android.miwotreff.db.DBConstants;
import de.micmun.android.miwotreff.db.DBDateUtility;
import de.micmun.android.miwotreff.db.DBProvider;
import de.micmun.android.miwotreff.recyclerview.DividerDecoration;
import de.micmun.android.miwotreff.recyclerview.Program;
import de.micmun.android.miwotreff.recyclerview.ProgramAdapter;
import de.micmun.android.miwotreff.recyclerview.RecyclerItemListener;

/**
 * Shows the Search Result.
 *
 * @author MicMun
 * @version 1.0, 01.02.2015
 */
public class SearchActivity
      extends BaseActivity
      implements RecyclerItemListener.RecyclerTouchListener {
   private static final int SEARCH_DATE = 0;
   private static final int SEARCH_OTHERS = 1;
   private static final String KEY_SEARCH = "search";

   private ProgramAdapter mAdapter;
   private RecyclerView mProgView;
   private DBProvider mDbProvider;

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setBackArrowEnabled(true);

      // initialize list view
      mProgView = (RecyclerView) findViewById(R.id.progListView);
      mAdapter = new ProgramAdapter();
      mAdapter.setHasStableIds(true);
      mProgView.setAdapter(mAdapter);
      mProgView.addOnItemTouchListener(new RecyclerItemListener(this, mProgView, this));
      LinearLayoutManager llm = new LinearLayoutManager(this);
      llm.setOrientation(LinearLayoutManager.VERTICAL);
      mProgView.setLayoutManager(llm);
      RecyclerView.ItemDecoration id = new DividerDecoration(ContextCompat
            .getDrawable(getApplicationContext(), R.drawable.recycler_divider));
      mProgView.addItemDecoration(id);

      // DBProvider
      mDbProvider = DBProvider.getInstance(this);

      Intent intent = getIntent();
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         handleIntent(query);
      }
   }

   @Override
   protected int getLayoutResource() {
      return R.layout.activity_main;
   }

   /**
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * @see android.app.Activity#onNewIntent(android.content.Intent)
    */
   @Override
   protected void onNewIntent(Intent intent) {
      setIntent(intent);
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query = intent.getStringExtra(SearchManager.QUERY);
         handleIntent(query);
      }
   }

   /**
    * Handle the intent with search.
    *
    * @param search Intent of the context.
    */
   private void handleIntent(String search) {
      String title = getResources().getString(R.string.search_title);
      title += " '" + search + "'";
      setTitle(title);

      Bundle b = new Bundle();
      b.putString(KEY_SEARCH, search);
      if (search.matches("^[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2,4}")) {
         querySearch(SEARCH_DATE, b);
      } else {
         querySearch(SEARCH_OTHERS, b);
      }
   }

   /**
    * @see android.app.ListActivity#onPause()
    */
   @Override
   public void onPause() {
      super.onPause();
   }

   /**
    * @see android.app.Activity#onResume()
    */
   @Override
   public void onResume() {
      super.onResume();
   }

   @Override
   public void onBackPressed() {
      // app icon in action bar clicked; go home
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      super.onBackPressed();
   }

   /**
    * Load the queried data for id and args.
    *
    * @param id   id of the search (SEARCH_DATE or SEARCH_OTHERS).
    * @param args Bundle with the search value.
    */
   public void querySearch(int id, Bundle args) {
      String sel = null;
      String[] selArgs = null;

      switch (id) {
         case SEARCH_DATE:
            Date d = DBDateUtility.getDateFromString(args.getString
                  (KEY_SEARCH));
            sel = DBConstants.KEY_DATUM + " = ?";
            selArgs = new String[]{String.valueOf(d.getTime())};
            break;
         case SEARCH_OTHERS:
            String s = args.getString(KEY_SEARCH);
            sel = "UPPER(" + DBConstants.KEY_THEMA + ") LIKE ? OR UPPER(" +
                  DBConstants.KEY_PERSON + ") LIKE ?";
            selArgs = new String[]{"%" + s + "%", "%" + s + "%"};
            break;
      }

      List<Program> programs = mDbProvider.queryProgram(sel, selArgs, null);
      mAdapter.setProgramList(programs);
   }

   @Override
   public void onClickItem(View v, int position) {

   }

   @Override
   public void onLongClickItem(View v, int position) {

   }
}
