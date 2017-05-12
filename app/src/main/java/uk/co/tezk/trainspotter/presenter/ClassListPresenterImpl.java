package uk.co.tezk.trainspotter.presenter;

import android.util.Log;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.network.NetworkModule;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 */

public class ClassListPresenterImpl implements IClassListPresenter.IPresenter {
    private static ClassListPresenterImpl presenter;

    IClassListPresenter.IView view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    @Inject
    ITrainSpotterInteractor interactor;

    // Implement Singelton implementation, shouldn't be needed when Dagger used

    public static ClassListPresenterImpl getInstance() {
        if (presenter == null) {
            NetworkModule net = new NetworkModule();
            //presenter = new ClassListPresenterImpl(new TrainSpotterInteractorImpl(new NetworkModule().provideApi(net.provideRetrofit(net.provideOkHttpclient(net.provideInterceptor())))));
            // Use the Dagger injected constructor
            presenter = new ClassListPresenterImpl();
        }
        return presenter;
    }

    // Default constructor, uses Dagger injection for interactor

    public ClassListPresenterImpl() {
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    // Various constructors to aid in testing, don't use injection here

    public ClassListPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public ClassListPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }

    @Override
    public void bind(IClassListPresenter.IView view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
    }

    @Override
    public void retrieveData() {
        Log.i("CLF"," obser on "+observeScheduler);
        Log.i("CLF"," subsc on "+subscribeScheduler);
        view.onStartLoading();
        interactor.getClassNumbers()
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .subscribe(new Observer<ClassNumbers>() {
                    @Override
                    public void onCompleted() {
                        view.onCompletedLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onErrorLoading(e==null?"Problem loading data":e.getMessage());
                    }

                    @Override
                    public void onNext(ClassNumbers classNumbers) {
                        view.showClassList(classNumbers.getClassNumbers());
                    }
                });
    }
}
