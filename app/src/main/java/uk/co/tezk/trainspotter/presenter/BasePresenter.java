package uk.co.tezk.trainspotter.presenter;

/**
 * Created by tezk on 11/05/17.
 */

public interface BasePresenter {
    public interface IView {

        public void onStartLoading() ;
        public void onErrorLoading(String message) ;
        public void onCompletedLoading() ;
    }

    public interface IPresenter {
        public void unbind() ;
        public void retrieveData() ;
    }
}
