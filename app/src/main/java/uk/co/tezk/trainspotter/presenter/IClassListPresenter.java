package uk.co.tezk.trainspotter.presenter;

import java.util.List;

import uk.co.tezk.trainspotter.model.ClassDetails;

/**
 * Presenter that fetches he list of Strings from Realm, includes more details than API returns
 */

public interface IClassListPresenter {
    interface IView {
        void showClassList(List<ClassDetails> classList);

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
