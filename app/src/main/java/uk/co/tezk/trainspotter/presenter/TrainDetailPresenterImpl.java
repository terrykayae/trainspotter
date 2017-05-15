package uk.co.tezk.trainspotter.presenter;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.network.NetworkModule;

/**
 * Created by tezk on 12/05/17.
 */

public class TrainDetailPresenterImpl implements ITrainDetailPresenter.IPresenter {
    private static TrainDetailPresenterImpl presenter;

    ITrainDetailPresenter.IView view;
    ITrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    // Singleton pattern, shouldn't need if using Dagger injection

    public static TrainDetailPresenterImpl getInstance() {
        if (presenter == null) {
            NetworkModule net = new NetworkModule();
            presenter = new TrainDetailPresenterImpl(new TrainSpotterInteractorImpl(new NetworkModule().provideApi(net.provideRetrofit(net.provideOkHttpclient(net.provideInterceptor())))));
        }
        return presenter;
    }

    // Various constructors to aid in testing

    public TrainDetailPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public TrainDetailPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }

    @Override
    public void bind(ITrainDetailPresenter.IView view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        this.view = null;
    }

    @Override
    public void retrieveData(String classNum, String engineNum) {
        view.onStartLoading();
        interactor.getTrainDetails(classNum, engineNum)
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .subscribe(new Observer<TrainDetail>() {
                    @Override
                    public void onCompleted() {
                        view.onCompletedLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onErrorLoading(e==null?"Problem loading data":e.getMessage());
                    }

                    @Override
                    public void onNext(TrainDetail trainDetail) {
                        view.showTrainDetails(trainDetail);
                    }
                });
    }
}