package uk.co.tezk.trainspotter.presenter;

/**
 * Created by tezk on 23/05/17.
 */

public interface IGeocodePresenter {
    interface IView {
        void updateLocation(String locationName) ;
    }

    interface IPresenter {
        void bind(IView view) ;
        void unBind() ;
        void getLocation(float lat, float lng, String apiKey) ;
    }
}
