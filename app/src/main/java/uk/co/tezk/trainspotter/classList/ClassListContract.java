package uk.co.tezk.trainspotter.classList;

import java.util.List;

import uk.co.tezk.trainspotter.model.ClassDetails;

/**
 * Presenter that fetches he list of Strings from Realm, includes more details than API returns
 */

public interface ClassListContract {
    interface View {
        void showClassList(List<ClassDetails> classList);

        void onStartLoading();

        void onErrorLoading(String message);

        void onCompletedLoading();

    }

    interface Presenter {
        void bind(View view);

        void unbind();

        void retrieveData();
    }
}
