package uk.co.tezk.trainspotter.presenter;

import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 11/05/17.
 */

public interface ITrainDetailPresenter extends BasePresenter {
    public interface IView {
        public void showTrainDetails(TrainDetail trainDetail) ;
    }

    public interface IPresenter {

    }
}
