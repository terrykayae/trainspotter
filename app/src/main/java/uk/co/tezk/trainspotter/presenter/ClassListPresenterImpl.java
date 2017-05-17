package uk.co.tezk.trainspotter.presenter;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.RealmTrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.ClassNumbers;

import static rx.Observable.concat;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 */

public class ClassListPresenterImpl implements IClassListPresenter.IPresenter {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    IClassListPresenter.IView view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    // API interactor
    @Inject
    ITrainSpotterInteractor interactor;
    // Caching interactor, accesses Realm database instead of API
    ITrainSpotterInteractor cachedInteractor;

    // Default constructor, uses Dagger injection for interactor

    public ClassListPresenterImpl() {
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
    }

    // Various constructors to aid in testing, don't use injection here

    public ClassListPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public ClassListPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
    }

    public ClassListPresenterImpl(
            ITrainSpotterInteractor interactor,
            Scheduler observeScheduler,
            Scheduler subscribeScheduler,
            ITrainSpotterInteractor cachedInteractor
    ) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
        this.cachedInteractor = cachedInteractor;
    }

    @Override
    public void retrieveData() {
        view.onStartLoading();
        compositeSubscription.add(concat(cachedInteractor.getClassNumbers(), interactor.getClassNumbers())
                .first()
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
    public void bind(IClassListPresenter.IView view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }
}
