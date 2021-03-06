package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.presenter.ClassListApiPresenterImpl;
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenterImpl;
import uk.co.tezk.trainspotter.presenter.TrainListPresenterImpl;

/**
 * Inject the modules into the classes
 */
@Component(modules = TrainSpotterInteractorModule.class ,dependencies = NetworkComponent.class)
public interface TrainSpotterInteractorComponent {
    void inject(ClassListPresenterImpl classListPresenter) ;
    void inject(ClassListApiPresenterImpl classListApiPresenter) ;
    void inject(TrainListPresenterImpl trainListPresenter) ;
    void inject(TrainDetailPresenterImpl trainDetailPresenter) ;
}
