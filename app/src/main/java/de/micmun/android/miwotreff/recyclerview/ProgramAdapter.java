package de.micmun.android.miwotreff.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.micmun.android.miwotreff.R;

/**
 * Adapter for the recyclerview with program entries.
 *
 * @author MicMun
 * @version 1.0, 29.12.16
 */

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewholder> {
   private List<Program> programList;

   /**
    * Creates an empty program adapter.
    */
   public ProgramAdapter() {
      programList = new ArrayList<>();
   }

   /**
    * Sets the program list.
    *
    * @param programList list of programs.
    */
   public void setProgramList(List<Program> programList) {
      this.programList.clear();
      this.programList = programList;
      notifyDataSetChanged();
   }

   @Override
   public ProgramViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
      return new ProgramViewholder(v);
   }

   @Override
   public void onBindViewHolder(ProgramViewholder holder, int position) {
      Program p = programList.get(position);
      holder.dateText.setText(p.getDateString());
      holder.topicText.setText(p.getTopic());
      holder.personText.setText(p.getPerson());

      if (p.isNextWednesDay()) {
         holder.view.setBackgroundDrawable(holder.view.getContext().getResources()
               .getDrawable(R.drawable.background_indicator_green));
      } else {
         holder.view.setBackgroundDrawable(holder.view.getContext().getResources()
               .getDrawable(R.drawable.background_indicator_transparent));
      }
   }

   @Override
   public long getItemId(int position) {
      return programList.get(position).get_id();
   }

   @Override
   public int getItemCount() {
      return programList.size();
   }

   /**
    * Viewholder for the textviews of one row.
    */
   class ProgramViewholder extends RecyclerView.ViewHolder {
      View view;
      TextView dateText;
      TextView topicText;
      TextView personText;

      /**
       * Creates a new ProgramViewholder with the view.
       *
       * @param itemView view for ProgramViewholder.
       */
      ProgramViewholder(View itemView) {
         super(itemView);
         view = itemView;
         dateText = (TextView) itemView.findViewById(R.id.text_datum);
         topicText = (TextView) itemView.findViewById(R.id.text_thema);
         personText = (TextView) itemView.findViewById(R.id.text_person);
      }
   }
}
