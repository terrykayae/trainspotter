package uk.co.tezk.trainspotter.geocode.injection;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.geocode.network.GeocodeRetrofit;
import uk.co.tezk.trainspotter.geocode.network.GeocoderInteractor;
import uk.co.tezk.trainspotter.geocode.network.GeocoderInteractorImpl;

/**
 * Injected via the TrainSpotterInteractorComponent
 */
@Module
public class GeocoderInteractorModule {
    @Provides
    GeocoderInteractor provideInteractor(GeocodeRetrofit retrofit) {
        return new GeocoderInteractorImpl(retrofit);
    }
}
