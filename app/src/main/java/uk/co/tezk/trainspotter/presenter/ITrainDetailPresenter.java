package uk.co.tezk.trainspotter.presenter;

import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 11/05/17.
 */

public interface ITrainDetailPresenter {
    public interface IView {
        void showTrainDetails(TrainDetail trainDetail);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    public interface IPresenter {
        void bind(IView view);

        void unbind();

        void retrieveData(String classNum, String engineNum);
    }
}
