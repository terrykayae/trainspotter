package uk.co.tezk.trainspotter.injection;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.interactor.GeocoderInteractorImpl;
import uk.co.tezk.trainspotter.interactor.IGeocoderInteractor;
import uk.co.tezk.trainspotter.network.IGeocodeRetrofit;

/**
 * Injected via the TrainSpotterInteractorComponent
 */
@Module
public class GeocoderInteractorModule {
    @Provides
    IGeocoderInteractor provideInteractor(IGeocodeRetrofit retrofit) {
        return new GeocoderInteractorImpl(retrofit);
    }
}
