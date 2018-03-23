package uk.co.tezk.trainspotter.history;

import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.tezk.trainspotter.model.SightingDetails;

public class HistoryPresenter implements HistoryPresenterContract.presenter {

    private Realm realm;

    private HistoryPresenterContract.View view;

    public HistoryPresenter(Realm realm) {
        this.realm = realm;
    }

    @Override
    public void bind(HistoryPresenterContract.View view) {
        this.view = view;
    }

    @Override
    public void unBind() {
        view = null;
    }

    @Override
    public void fetchItems() {
        RealmResults<SightingDetails> results = realm.where(SightingDetails.class).findAll();
        view.showItems(results);
    }
}
