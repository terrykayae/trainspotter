package uk.co.tezk.trainspotter.presenter;

import java.util.List;

/**
 * API for presenter that fetches he list of Strings from the API
 */

public interface IClassListApiPresenter {
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
