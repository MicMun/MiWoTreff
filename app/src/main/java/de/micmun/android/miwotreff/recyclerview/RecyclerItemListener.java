package de.micmun.android.miwotreff.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listener for item click and long click events.
 *
 * @author MicMun
 * @version 1.0, 29.12.16
 */

public class RecyclerItemListener implements RecyclerView.OnItemTouchListener {
   private GestureDetector gd;

   /**
    * Creates a new RecyclerItemListener with context, recyclerviw and RecyclerTouchListener.
    *
    * @param ctx      context.
    * @param rv       recyclerview.
    * @param listener RecyclerTouchListener.
    */
   public RecyclerItemListener(Context ctx, final RecyclerView rv,
                               final RecyclerTouchListener listener) {
      gd = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
         @Override
         public void onLongPress(MotionEvent e) {
            // We find the view
            View v = rv.findChildViewUnder(e.getX(), e.getY());
            // Notify the even
            listener.onLongClickItem(v, rv.getChildAdapterPosition(v));
         }

         @Override
         public boolean onSingleTapUp(MotionEvent e) {
            View v = rv.findChildViewUnder(e.getX(), e.getY());
            // Notify the even
            listener.onClickItem(v, rv.getChildAdapterPosition(v));
            return true;
         }
      });
   }

   @Override
   public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
      View child = rv.findChildViewUnder(e.getX(), e.getY());
      return (child != null && gd.onTouchEvent(e));
   }

   @Override
   public void onTouchEvent(RecyclerView rv, MotionEvent e) {

   }

   @Override
   public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

   }

   /**
    * Interface for onClick and onLongClick.
    */
   public interface RecyclerTouchListener {
      /**
       * Handles the click event on a view at position.
       *
       * @param v        clicked view.
       * @param position position.
       */
      void onClickItem(View v, int position);

      /**
       * Handles the long click event on a view at position.
       *
       * @param v        long clicked view.
       * @param position position.
       */
      void onLongClickItem(View v, int position);
   }
}
