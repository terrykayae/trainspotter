package uk.co.tezk.trainspotter.presenter;

import java.util.List;

import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Created by tezk on 11/05/17.
 */

public interface ITrainListPresenterContract extends BasePresenter {
    public interface IView {
        public void showTrainList(List<TrainListItem> trainList) ;

    }

    public interface IPresenter {

    }

}
