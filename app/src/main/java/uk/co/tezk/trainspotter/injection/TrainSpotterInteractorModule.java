package uk.co.tezk.trainspotter.injection;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;

/**
 * Created by tezk on 12/05/17.
 */
@Module
public class TrainSpotterInteractorModule {
    @Provides
    ITrainSpotterInteractor provideInteractor(ITrainSpottingRetrofit retrofit) {
        return new TrainSpotterInteractorImpl(retrofit, false);
    }
}
