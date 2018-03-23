package uk.co.tezk.trainspotter.presenter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Interface to allow locationUpdate to the map from the LocationProvied
 */

public interface LocationUpdateContract {
    public interface View {
        public void updatePosition(LatLng lanLng) ;
    }

    public interface Presenter {
        public void bind(View view) ;
        public void unBind() ;
        public void getLocation() ;
    }
}
