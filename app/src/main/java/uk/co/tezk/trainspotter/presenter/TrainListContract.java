package uk.co.tezk.trainspotter.presenter;

import java.util.List;

import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 11/05/17.
 */

public interface TrainListContract {
    public interface View {
        void showTrainList(List<TrainDetail> trainList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    interface Presenter {
        void bind(View view);

        void unbind();

        void retrieveData(String classNum);

        void performSearch(String searchString) ;
    }

}
