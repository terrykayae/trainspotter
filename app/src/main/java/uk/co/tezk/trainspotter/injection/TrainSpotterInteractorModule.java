package uk.co.tezk.trainspotter.injection;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.network.TrainSpottingRetrofit;

/**
 * Created by tezk on 12/05/17.
 */
@Module
public class TrainSpotterInteractorModule {
    @Provides
    TrainSpotterInteractor provideInteractor(TrainSpottingRetrofit retrofit) {
        return new TrainSpotterInteractorImpl(retrofit);
    }
}
