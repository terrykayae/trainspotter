package uk.co.tezk.trainspotter.presenter;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.ClassNumbers;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 */

public class ClassListApiPresenterImpl implements IClassListApiPresenter.IPresenter {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    IClassListApiPresenter.IView view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    // API apiInteractor
    @Inject
    ITrainSpotterInteractor interactor;

    // Default constructor, uses Dagger injection for apiInteractor

    public ClassListApiPresenterImpl() {
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    // Various constructors to aid in testing, don't use injection here

    public ClassListApiPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public ClassListApiPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
    }

    public ClassListApiPresenterImpl(
            ITrainSpotterInteractor interactor,
            Scheduler observeScheduler,
            Scheduler subscribeScheduler,
            ITrainSpotterInteractor cachedInteractor
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
    public void bind(IClassListApiPresenter.IView view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }
}
