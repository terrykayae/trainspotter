package uk.co.tezk.trainspotter.presenter;

import java.util.List;

/**
 * Created by tezk on 11/05/17.
 */

public interface IClassListPresenter {
    interface IView {
        void showClassList(List<String> classList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();

    }

    interface IPresenter {
        void bind(IView view);

        void unbind();

        void retrieveData();
    }
}
