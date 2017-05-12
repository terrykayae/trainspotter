package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.model.ClassDetails;

/**
 * Created by tezk on 12/05/17.
 */

public class ClassListRecyclerViewAdapter extends RecyclerView.Adapter <ClassListRecyclerViewAdapter.ClassViewHolder> {
    // Interface for Fragment to implement to listen for clicks
    interface OnClassListItemClickListener {
        void onItemClick(String classId, boolean longClick);
    }
    OnClassListItemClickListener clickListener;
    Context context;

    private List<ClassDetails> classesList;

    public ClassListRecyclerViewAdapter(List <ClassDetails> classesList, OnClassListItemClickListener clickListener, Context context) {
        this.classesList = classesList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_list_card_layout_item, parent, false);
        return new ClassListRecyclerViewAdapter.ClassViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return classesList.size();
    }

    @Override
    public void onBindViewHolder(final ClassViewHolder holder, int position) {
        holder.tvClassNum.setText(classesList.get(position).getClassId());
        int sightingCount = classesList.get(position).getSightingsRecorded()==null?0:classesList.get(position).getSightingsRecorded();
        if (sightingCount == 0) {
            holder.tvSightingCount.setText(context.getString(R.string.no_sightings_logged));
        } else {
            holder.tvSightingCount.setText(
                    String.format(context.getString(R.string.sighting_count_message), sightingCount, classesList.get(position).getTotalTrains())
            );
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener!=null) {
                    clickListener.onItemClick(holder.tvClassNum.getText().toString(), false);
                }

            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (clickListener!=null) {
                    clickListener.onItemClick(holder.tvClassNum.getText().toString(), true);
                }

                return false;
            }
        });
    }

    public class ClassViewHolder extends RecyclerView.ViewHolder {
        public TextView tvClassNum;
        public TextView tvSightingCount;
        View mView;

        public ClassViewHolder(View view) {
            super(view);
            mView = view;
            tvClassNum = (TextView) view.findViewById(R.id.tvClassNumber);
            tvSightingCount = (TextView) view.findViewById(R.id.tvClassSightingCount);
        }
    }
}
