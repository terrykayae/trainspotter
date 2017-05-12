package uk.co.tezk.trainspotter.presenter;

import android.util.Log;

import java.util.List;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.NetworkModule;

/**
 * Presenter for dealing with the overall list of trains
 */

public class TrainListPresenterImpl implements ITrainListPresenter.IPresenter {
    private static TrainListPresenterImpl presenter;

    ITrainListPresenter.IView view;
    ITrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    // Singleton pattern, shouldn't need if using Dagger injection

    public static TrainListPresenterImpl getInstance() {
        if (presenter == null) {
            NetworkModule net = new NetworkModule();
            presenter = new TrainListPresenterImpl(new TrainSpotterInteractorImpl(new NetworkModule().provideApi(net.provideRetrofit(net.provideOkHttpclient(net.provideInterceptor())))));
        }
        return presenter;
    }

    // Various constructors to aid in testing

    public TrainListPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public TrainListPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }


    @Override
    public void unbind() {
        view = null;
    }

    @Override
    public void retrieveData(String classNumber) {

        view.onStartLoading();
        interactor.getTrains(classNumber)
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .subscribe(new Observer<List<TrainListItem>>() {
                    @Override
                    public void onCompleted() {
                        view.onCompletedLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onErrorLoading(e==null?"Problem loading data":e.getMessage());
                    }

                    @Override
                    public void onNext(List <TrainListItem> trainList) {
                        view.showTrainList(trainList);
                    }
                });
    }

    @Override
    public void bind(ITrainListPresenter.IView view) {
        this.view = view;
    }
}
