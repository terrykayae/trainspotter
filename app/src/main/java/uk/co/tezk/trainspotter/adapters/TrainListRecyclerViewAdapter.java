package uk.co.tezk.trainspotter.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Created by tezk on 15/05/17.
 */

public class TrainListRecyclerViewAdapter extends RecyclerView.Adapter <TrainListRecyclerViewAdapter.TrainViewHolder> {
    // Interface for Fragment to implement to listen for clicks
    public interface OnTrainListItemClickListener {
        void onItemClick(String classId, String trainNum, boolean longClick);
    }
    OnTrainListItemClickListener clickListener;
    Context context;

    private List<TrainDetail> trainList;

    public TrainListRecyclerViewAdapter(List <TrainDetail> trainList, OnTrainListItemClickListener clickListener, Context context) {
        this.trainList = trainList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public TrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.train_list_card_layout_item, parent, false);
        return new TrainListRecyclerViewAdapter.TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrainViewHolder holder, int position) {
        final TrainListItem trainListItem = trainList.get(position).getTrain();
        List <SightingDetails> sightings = trainList.get(position).getSightings();
        if (sightings == null || sightings.size()==0) {
            // No sightings yet reported, hide the last sighting text views

            holder.tvTrainLastSpotted.setVisibility(View.GONE);
            holder.tvTrainWhere.setVisibility(View.GONE);
            // Hide the labels
            holder.tvTrainLastSpottedLabel.setVisibility(View.GONE);
            holder.tvTrainWhereLabel.setVisibility(View.GONE);
        } else {
            // find if we've spotted it and where last spotted
            boolean spotted = false;
            SightingDetails mostRecent = sightings.get(0);
            for (SightingDetails eachSighting : sightings) {
                if (eachSighting.getDate().compareTo(mostRecent.getDate()) > 0) {
                    mostRecent = eachSighting;
                }
                // If the sighting has a time recorded, it was made by the user
                if (eachSighting.getTime()!=null)
                    spotted = true;
            }
            if (spotted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.ivTrainSighting.setImageDrawable(context.getDrawable(R.drawable.btn_check_buttonless_on));
                }
                holder.tvTrainLastSpotted.setText(mostRecent.getDate());
                // Set the where to a name, if we know it, or location
                if (mostRecent.getLocationName()!=null) {
                    holder.tvTrainWhere.setText(mostRecent.getLocationName());
                } else {
                    holder.tvTrainWhere.setText(mostRecent.getLat()+", "+mostRecent.getLon());
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    holder.ivTrainSighting.setImageDrawable(context.getDrawable(android.R.drawable.ic_menu_view));
            }
        }
        holder.tvTrainNumber.setText(trainListItem.getNumber());
        holder.tvTrainName.setText(trainListItem.getName());
        holder.tvTrainDepot.setText(trainListItem.getDepot());
        holder.tvTrainLivery.setText(trainListItem.getLivery());
        holder.trainListItem = trainListItem;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener!=null) {
                    clickListener.onItemClick(trainListItem.getClass_(), trainListItem.getNumber(), false);
                }
            }
        });

        holder.ivTrainSighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Adding log", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return trainList.size();
    }

    public class TrainViewHolder extends RecyclerView.ViewHolder {
        View view;
        TrainListItem trainListItem;
        @BindView (R.id.tvTrainId) TextView tvTrainNumber;
        @BindView (R.id.tvTrainName)TextView tvTrainName;
        @BindView (R.id.tvTrainDepot)TextView tvTrainDepot;
        @BindView (R.id.tvTrainLivery)TextView tvTrainLivery;
        @BindView (R.id.tvTrainLastSpotted)TextView tvTrainLastSpotted;
        @BindView (R.id.tvTrainWhere)TextView tvTrainWhere;
        @BindView (R.id.ivTrainSighting) ImageView ivTrainSighting;

        // holders for the labels that we may want to hide
        @BindView (R.id.tvTrainLastSpottedLabel) TextView tvTrainLastSpottedLabel;
        @BindView(R.id.tvTrainWhereLabel) TextView tvTrainWhereLabel;

        public TrainViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            ButterKnife.bind(this, view);
        }
    }
}
