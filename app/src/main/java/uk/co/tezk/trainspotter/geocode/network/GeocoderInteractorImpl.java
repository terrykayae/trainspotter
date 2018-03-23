package uk.co.tezk.trainspotter.geocode.network;

import rx.Observable;
import uk.co.tezk.trainspotter.geocode.model.Geocoder;

/**
 * Created by tezk on 23/05/17.
 */

public class GeocoderInteractorImpl implements GeocoderInteractor {
    GeocodeRetrofit geocodeRetrofit;

    public GeocoderInteractorImpl(GeocodeRetrofit geocodeRetrofit) { this.geocodeRetrofit = geocodeRetrofit; }

    @Override
    public Observable<Geocoder> getGeocoded(float lat, float lng, String apiKey) {
        return geocodeRetrofit.getGeocoded(lat+"+"+lng, apiKey);
    }
}
