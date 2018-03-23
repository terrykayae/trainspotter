package uk.co.tezk.trainspotter.presenter;

import android.widget.ImageView;

import java.util.List;

/**
 * Created by tezk on 12/05/17.
 */

public interface TrainImageContract {
    interface View {
        void showTrainImages(List<ImageView> imageList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    interface Presenter {
        void bind(View view) ;
        void unBind() ;
        void retrieveImages(String classId, String engineId) ;
    }
}
