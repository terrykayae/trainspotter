package uk.co.tezk.trainspotter.presenter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Interface to allow locationUpdate to the map from the LocationProvied
 */

public interface ILocationUpdatePresenter {
    public interface IView {
        public void updatePosition(LatLng lanLng) ;
    }

    public interface IPresenter {
        public void bind(IView view) ;
        public void unBind() ;
        public void getLocation() ;
    }
}
