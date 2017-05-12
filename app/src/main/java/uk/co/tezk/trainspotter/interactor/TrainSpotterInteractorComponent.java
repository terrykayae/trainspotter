package uk.co.tezk.trainspotter.interactor;

import dagger.Component;
import uk.co.tezk.trainspotter.network.NetworkComponent;
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;

/**
 * Created by tezk on 12/05/17.
 */
@Component(modules = TrainSpotterInteractorModule.class ,dependencies = NetworkComponent.class)
public interface TrainSpotterInteractorComponent {
    void inject(ClassListPresenterImpl classListPresenter) ;
}
