package uk.co.tezk.trainspotter.presenter;

import android.widget.ImageView;

import java.util.List;

/**
 * Created by tezk on 12/05/17.
 */

public interface ITrainImagePresenter {
    interface IView {
        void showTrainImages(List<ImageView> imageList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();
    }

    interface IPresenter {
        void bind(IView view) ;
        void unBind() ;
        void retrieveImages(String classId, String engineId) ;
    }
}
