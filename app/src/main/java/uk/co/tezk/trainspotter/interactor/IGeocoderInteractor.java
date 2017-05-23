package uk.co.tezk.trainspotter.interactor;

import rx.Observable;
import uk.co.tezk.trainspotter.model.geocoder.Geocoder;

/**
 * Created by tezk on 23/05/17.
 */

public interface IGeocoderInteractor {
    public Observable<Geocoder> getGeocoded(float lat, float lng, String apiKey) ;
}
