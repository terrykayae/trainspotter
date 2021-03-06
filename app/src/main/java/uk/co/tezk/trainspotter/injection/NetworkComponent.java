package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.network.IGeocodeRetrofit;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;
import uk.co.tezk.trainspotter.network.Submitter;

/**
 * Create the injections
 */
@Component(modules = NetworkModule.class)
public interface NetworkComponent {
    ITrainSpottingRetrofit provideApi();
    IGeocodeRetrofit provideGeocoder();
    void inject(Submitter submitter);
    //void inject(ClassListPresenterImpl classListPresenter) ;
    //void inject(TrainListPresenterImpl trainListPresenter) ;
    //void inject(TrainDetailPresenterImpl trainDetailPresenter) ;
    //void inject(TrainImagePresenterImpl trainImagePresenter) ;
}
