package uk.co.tezk.trainspotter.geocode;

/**
 * Created by tezk on 23/05/17.
 */

public interface GeocodeContract {
    interface View {
        void updateLocation(String locationName) ;
    }

    interface Presenter {
        void bind(View view) ;
        void unBind() ;
        void getLocation(float lat, float lng, String apiKey) ;
    }
}
