package uk.co.tezk.trainspotter.classList;

import java.util.List;

/**
 * API for presenter that fetches he list of Strings from the API
 */

public interface ClassListApiContract {
    interface View {
        void showClassList(List<String> classList);

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
