package uk.co.tezk.trainspotter.injection;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.classList.ClassListApiContract;
import uk.co.tezk.trainspotter.classList.ClassListApiPresenter;
import uk.co.tezk.trainspotter.classList.ClassListContract;
import uk.co.tezk.trainspotter.classList.ClassListPresenter;
import uk.co.tezk.trainspotter.presenter.TrainDetailContract;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenter;
import uk.co.tezk.trainspotter.presenter.TrainListContract;
import uk.co.tezk.trainspotter.presenter.TrainListPresenter;

/**
 * Created by tezk on 10/03/18.
 */

@Module
public class PresentersModule {
    @Provides
    ClassListApiContract.Presenter providesClassListApiPresenter(TrainSpotterInteractor interactor) {
        return new ClassListApiPresenter(interactor);
    }

    @Provides
    TrainListContract.Presenter provideTrainListPresenter(TrainSpotterInteractor interactor) {
        return new TrainListPresenter(interactor);
    }
    @Provides
    ClassListContract.Presenter provideClassListPresenter(ClassListApiContract.Presenter presenter) {
        return new ClassListPresenter(presenter, true);
    }

    @Provides
    TrainDetailContract.Presenter provideTrainDetailPresenter(TrainSpotterInteractor interactor) {
        return new TrainDetailPresenter(interactor);
    }
}
