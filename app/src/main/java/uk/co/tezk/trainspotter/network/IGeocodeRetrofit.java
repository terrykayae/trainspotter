package uk.co.tezk.trainspotter.network;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.geocoder.Geocoder;

/**
 * Created by tezk on 23/05/17.
 */

public interface IGeocodeRetrofit {
    @GET(Constant.GEOCODE_BASE_URL+Constant.GEOCODE_API)
    public Observable<Geocoder> getGeocoded(@Query(value = "q", encoded = true) String latLng, @Query("key") String apiKey) ; // latLng string in format lat+lng
}
