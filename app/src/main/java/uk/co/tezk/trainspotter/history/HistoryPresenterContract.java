package uk.co.tezk.trainspotter.history;

import java.util.List;

import uk.co.tezk.trainspotter.model.SightingDetails;

public interface HistoryPresenterContract {
    interface View {
        void onError() ;
        void showItems(List<SightingDetails> sightings) ;
        void showLoading() ;
        void hideLoading() ;
    }

    interface presenter {
        void bind(View view) ;
        void unBind() ;
        void fetchItems() ;
    }
}
