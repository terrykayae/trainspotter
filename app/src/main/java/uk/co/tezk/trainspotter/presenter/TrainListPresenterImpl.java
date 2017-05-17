package uk.co.tezk.trainspotter.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmResults;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.RealmTrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static rx.Observable.concat;

/**
 * Presenter for dealing with the overall list of trains
 */

public class TrainListPresenterImpl implements ITrainListPresenter.IPresenter {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    ITrainListPresenter.IView view;
    @Inject
    ITrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;
    private List<TrainDetail> trainDetailList;
    // Caching interactor, accesses Realm database instead of API
    ITrainSpotterInteractor cachedInteractor;

    public TrainListPresenterImpl() {
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
    }

    // Various constructors to aid in testing

    public TrainListPresenterImpl(ITrainSpotterInteractor interactor) {
        this(interactor, AndroidSchedulers.mainThread(), Schedulers.io());
    }

    public TrainListPresenterImpl(ITrainSpotterInteractor interactor, Scheduler observeScheduler, Scheduler subscribeScheduler) {
        this.interactor = interactor;
        this.observeScheduler = observeScheduler;
        this.subscribeScheduler = subscribeScheduler;
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
    }

    public TrainListPresenterImpl(
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
    public void retrieveData(String classNumber) {
        //TODO :Not thread safe due to global list used

        view.onStartLoading();
        trainDetailList = new ArrayList<>();
        // Get list of trains (from API or Realm) then combine with our created and cached Realm data of train
        // Details
        // TODO : Change the realm here to use a mockable object
        compositeSubscription.add(concat(cachedInteractor.getTrains(classNumber), interactor.getTrains(classNumber))
                .first()
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .flatMap(new Func1<List<TrainListItem>, Observable<TrainListItem>>() {
                    @Override
                    public Observable<TrainListItem> call(List<TrainListItem> trainListItems) {
                        return Observable.from(trainListItems);
                    }
                })
                .map(new Func1<TrainListItem, TrainDetail>() {
                    @Override
                    public TrainDetail call(TrainListItem trainListItem) {
                        TrainDetail newTrain = new TrainDetail();
                        newTrain.setTrain(trainListItem);
                        RealmResults<SightingDetails> results = RealmHandler.getInstance().getSightings(trainListItem.getClass_(), trainListItem.getNumber());
                        newTrain.setSightings(new ArrayList());
                        newTrain.getSightings().addAll(results);

                        return newTrain;
                    }
                })
                .subscribe(new Observer<TrainDetail>() {
                    @Override
                    public void onCompleted() {
                        view.showTrainList(trainDetailList);
                        view.onCompletedLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.onErrorLoading(e == null ? "Problem loading data" : e.getMessage());
                    }

                    @Override
                    public void onNext(TrainDetail trainDetail) {
                        trainDetailList.add(trainDetail);
                    }
                }));
    }

    @Override
    public void bind(ITrainListPresenter.IView view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription != null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }
}
