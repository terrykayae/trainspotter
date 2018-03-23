package uk.co.tezk.trainspotter.presenter;

import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 11/05/17.
 */

public interface TrainDetailContract {
    public interface View {
        void showTrainDetails(TrainDetail trainDetail);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    public interface Presenter {
        void bind(View view);

        void unbind();

        void retrieveData(String classNum, String engineNum);
    }
}
