package uk.co.tezk.trainspotter.geocode.network;

import rx.Observable;
import uk.co.tezk.trainspotter.geocode.model.Geocoder;

/**
 * Created by tezk on 23/05/17.
 */

public interface GeocoderInteractor {
    public Observable<Geocoder> getGeocoded(float lat, float lng, String apiKey) ;
}
