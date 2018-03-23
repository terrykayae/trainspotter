package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.classList.ClassListApiPresenter;
import uk.co.tezk.trainspotter.classList.ClassListPresenter;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenter;
import uk.co.tezk.trainspotter.presenter.TrainListPresenter;

@Component(modules = TrainSpotterInteractorModule.class ,dependencies = NetworkComponent.class)
public interface TrainSpotterInteractorComponent {

    TrainSpotterInteractor provideInteractor() ;

    void inject(ClassListPresenter classListPresenter) ;
    void inject(ClassListApiPresenter classListApiPresenter) ;
    void inject(TrainListPresenter trainListPresenter) ;
    void inject(TrainDetailPresenter trainDetailPresenter) ;
}
