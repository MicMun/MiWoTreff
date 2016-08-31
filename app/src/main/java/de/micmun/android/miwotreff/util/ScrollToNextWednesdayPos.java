package de.micmun.android.miwotreff.util;

import android.os.AsyncTask;
import android.widget.ListView;

/**
 * AsyncTask to calculate position of next wednesday and scroll the list view to this position.
 *
 * @author MicMun
 * @version 1.0, 31.08.2016
 */
public class ScrollToNextWednesdayPos extends AsyncTask<Void, Void, Integer> {
   private ListView mListView;
   private SpecialCursorAdapter mAdapter;

   private int mOffset = 8;

   /**
    * Creates a new ScrollToNextWednesdayPos with list view and date of next wednesday.
    *
    * @param listView list view to scroll.
    */
   public ScrollToNextWednesdayPos(ListView listView) {
      mListView = listView;
      mAdapter = (SpecialCursorAdapter) mListView.getAdapter();

      mOffset = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition() + 1;
   }

   @Override
   protected Integer doInBackground(Void... voids) {
      int pos = mAdapter.getmNextWdPos();

      while (pos == -1) {
         publishProgress();
         pos = mAdapter.getmNextWdPos();
      }

      return pos;
   }

   @Override
   protected void onProgressUpdate(Void... values) {
      super.onProgressUpdate(values);

      mListView.smoothScrollByOffset(mOffset);
   }

   @Override
   protected void onPostExecute(Integer result) {
      super.onPostExecute(result);
      mListView.smoothScrollToPosition(result);
   }
}
