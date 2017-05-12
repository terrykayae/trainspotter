package uk.co.tezk.trainspotter.interactor;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;

/**
 * Created by tezk on 12/05/17.
 */
@Module
public class TrainSpotterInteractorModule {
    @Provides
    ITrainSpotterInteractor provideInteractor(ITrainSpottingRetrofit retrofit) {
        return new TrainSpotterInteractorImpl(retrofit);
    }
}
