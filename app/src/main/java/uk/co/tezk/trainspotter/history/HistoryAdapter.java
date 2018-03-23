package uk.co.tezk.trainspotter.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import uk.co.tezk.trainspotter.model.SightingDetails;

public class HistoryAdapter extends RecyclerView.Adapter <HistoryAdapter.HistoryViewHolder>  {

    List<SightingDetails> sightings;

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return sightings.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        public HistoryViewHolder(View itemView) {
            super(itemView);
        }

        
    }
}
