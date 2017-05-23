package uk.co.tezk.trainspotter.presenter;

import android.util.Log;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.RealmTrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 12/05/17.
 */

public class TrainDetailPresenterImpl implements ITrainDetailPresenter.IPresenter {
    private static TrainDetailPresenterImpl presenter;

    ITrainDetailPresenter.IView view;
    // Interactor for the API
    @Inject
    ITrainSpotterInteractor apiInteractor;
    // Interactor for the Realm data
    ITrainSpotterInteractor cachedInteractor;

    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    // Various constructors to aid in testing
    public TrainDetailPresenterImpl() {
        // Dagger inject for apiInteractor
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        // Cached interactor - reads from Realm
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    public TrainDetailPresenterImpl(ITrainSpotterInteractor apiInteractor) {
        this(apiInteractor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public TrainDetailPresenterImpl(ITrainSpotterInteractor apiInteractor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.apiInteractor = apiInteractor;
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
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
        Log.i("TDPI", "unBind called");
    }

    @Override
    public void retrieveData(String classNum, String trainNum) {
        // Fetch the sightings from the API and merge with any stored sightings and our images
        // Cannot merge data from Realm as it can't be altered outside of a transaction
        // plus I don't want any changes persisting!
        view.onStartLoading();
        Log.i("TDPI", "compositeSubscription = "+compositeSubscription);
        Log.i("TDPI", "compositeSubscription subscribed = "+compositeSubscription);

        Observable<TrainDetail> cachedTrainDetails = cachedInteractor.getTrainDetails(classNum, trainNum);
        Observable<TrainDetail> apiTrainDetails = apiInteractor.getTrainDetails(classNum, trainNum);

        // By concating the two, showTrainDetails will get called twice, once with the cached data, once with api data...
        compositeSubscription.add(Observable.concat(cachedTrainDetails, apiTrainDetails)
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
                }));
        /*
        compositeSubscription.add(apiInteractor.getTrainDetails(classNum, trainNum)
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
                }));
                */
    }
}
