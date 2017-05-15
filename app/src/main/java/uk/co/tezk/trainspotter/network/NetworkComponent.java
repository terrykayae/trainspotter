package uk.co.tezk.trainspotter.network;

import dagger.Component;

/**
 * Create the injections
 */
@Component(modules = NetworkModule.class)
public interface NetworkComponent {
    ITrainSpottingRetrofit provideApi();
    void inject(Submitters submitters);
    //void inject(ClassListPresenterImpl classListPresenter) ;
    //void inject(TrainListPresenterImpl trainListPresenter) ;
    //void inject(TrainDetailPresenterImpl trainDetailPresenter) ;
    //void inject(TrainImagePresenterImpl trainImagePresenter) ;
}
