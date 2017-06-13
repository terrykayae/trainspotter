package uk.co.tezk.trainspotter.presenter;

import java.util.List;

import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 11/05/17.
 */

public interface ITrainListPresenter {
    public interface IView {
        void showTrainList(List<TrainDetail> trainList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    interface IPresenter {
        void bind(IView view);

        void unbind();

        void retrieveData(String classNum);

        void performSearch(String searchString) ;
    }

}
