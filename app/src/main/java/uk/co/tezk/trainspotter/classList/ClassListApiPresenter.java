package uk.co.tezk.trainspotter.classList;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.ClassNumbers;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 */

public class ClassListApiPresenter implements ClassListApiContract.Presenter {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    ClassListApiContract.View view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    // API apiInteractor
    @Inject
    TrainSpotterInteractor interactor;

    // Default constructor, uses Dagger injection for apiInteractor

    public ClassListApiPresenter() {
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    // Various constructors to aid in testing, don't use injection here

    public ClassListApiPresenter(TrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public ClassListApiPresenter(TrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }

    public ClassListApiPresenter(
            TrainSpotterInteractor interactor,
            Scheduler observeScheduler,
            Scheduler subscribeScheduler,
            TrainSpotterInteractor cachedInteractor
    ) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }

    @Override
    public void retrieveData() {
        view.onStartLoading();
        compositeSubscription.add(interactor.getClassNumbers()
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .timeout(10, TimeUnit.SECONDS)
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
                }));
    }

    @Override
    public void bind(ClassListApiContract.View view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }
}
