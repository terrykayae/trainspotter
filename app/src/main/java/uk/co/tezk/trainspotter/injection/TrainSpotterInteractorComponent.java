package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenterImpl;
import uk.co.tezk.trainspotter.presenter.TrainListPresenterImpl;

/**
 * Created by tezk on 12/05/17.
 */
@Component(modules = TrainSpotterInteractorModule.class ,dependencies = NetworkComponent.class)
public interface TrainSpotterInteractorComponent {
    void inject(ClassListPresenterImpl classListPresenter) ;
    void inject(TrainListPresenterImpl trainListPresenter) ;
    void inject(TrainDetailPresenterImpl trainDetailPresenter) ;
}
