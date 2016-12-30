package de.micmun.android.miwotreff.recyclerview;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Decoration of the recyclerview with a divider.
 *
 * @author MicMun
 * @version 1.0, 29.12.16
 */

public class DividerDecoration extends RecyclerView.ItemDecoration {
   private Drawable mDivider;

   /**
    * Creates a new DividerDecoration with a divider.
    *
    * @param divider divider for decoration.
    */
   public DividerDecoration(Drawable divider) {
      this.mDivider = divider;
   }

   @Override
   public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
      final int left = parent.getPaddingLeft();
      final int right = parent.getWidth() - parent.getPaddingRight();

      final int childCount = parent.getChildCount();
      for (int i = 0; i < childCount; i++) {
         final View child = parent.getChildAt(i);
         final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
               .getLayoutParams();
         final int top = child.getBottom() + params.bottomMargin;
         final int bottom = top + mDivider.getIntrinsicHeight();
         mDivider.setBounds(left, top, right, bottom);
         mDivider.draw(canvas);
      }
   }
}
