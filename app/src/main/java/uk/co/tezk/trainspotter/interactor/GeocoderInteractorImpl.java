package uk.co.tezk.trainspotter.interactor;

import rx.Observable;
import uk.co.tezk.trainspotter.model.geocoder.Geocoder;
import uk.co.tezk.trainspotter.network.IGeocodeRetrofit;

/**
 * Created by tezk on 23/05/17.
 */

public class GeocoderInteractorImpl implements IGeocoderInteractor {
    IGeocodeRetrofit geocodeRetrofit;

    public GeocoderInteractorImpl(IGeocodeRetrofit geocodeRetrofit) { this.geocodeRetrofit = geocodeRetrofit; }

    @Override
    public Observable<Geocoder> getGeocoded(float lat, float lng, String apiKey) {
        return geocodeRetrofit.getGeocoded(lat+"+"+lng, apiKey);
    }
}
