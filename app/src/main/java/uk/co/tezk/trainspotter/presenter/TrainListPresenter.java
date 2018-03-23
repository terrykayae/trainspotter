package uk.co.tezk.trainspotter.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.realm.RealmResults;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.realm.RealmTrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static android.util.Log.i;
import static rx.Observable.concat;

/**
 * Presenter for dealing with the overall list of trains
 */

public class TrainListPresenter implements TrainListContract.Presenter {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    TrainListContract.View view;
    @Inject
    TrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;
    private List<TrainDetail> trainDetailList;
    // Caching apiInteractor, accesses Realm database instead of API
    TrainSpotterInteractor cachedInteractor;

    public TrainListPresenter(TrainSpotterInteractor interactor) {
        this.interactor = interactor;
        cachedInteractor = new RealmTrainSpotterInteractorImpl();
    }

    @Override
    public void retrieveData(String classNumber) {
        //TODO : Possibly not thread safe due to global list used

        view.onStartLoading();
        trainDetailList = new ArrayList<>();
        // Get list of trains (from API or Realm) then combine with our created and cached Realm data of train
        // Details
        compositeSubscription.add(concat(cachedInteractor.getTrains(classNumber), interactor.getTrains(classNumber))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .timeout(20, TimeUnit.SECONDS)
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
                        RealmResults<SightingDetails> results = RealmHandler.getInstance()
                                .getSightings(trainListItem.getClass_(), trainListItem.getNumber());
                        newTrain.setSightings(new ArrayList());
                        newTrain.getSightings().addAll(results);

                        return newTrain;
                    }
                })
                .subscribe(new Observer<TrainDetail>() {
                    @Override
                    public void onCompleted() {
                        i("TLPI", "Observable onCompleted");
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
    public void performSearch(String searchString) {
        RealmHandler handler = RealmHandler.getInstance();
        List<TrainDetail> list = handler.performSearch(searchString);
        i("TLPI", "list = " + list);
        compositeSubscription.add(Observable.from(RealmHandler.getInstance().performSearch(searchString))
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .toList()
                .subscribe(new Observer<List<TrainDetail>>() {
                    @Override
                    public void onCompleted() {
                        //      view.onCompletedLoading();
                        i("TLPI", "search onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        //        view.onErrorLoading(e.getMessage());
                        i("TLPI", "search error " + e.getMessage());
                    }

                    @Override
                    public void onNext(List<TrainDetail> trainDetail) {
                        view.showTrainList(trainDetail);
                        i("TLPI", "search showing " + trainDetail);
                    }
                }));
    }

    @Override
    public void bind(TrainListContract.View view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription != null)
            compositeSubscription.clear();
    }
}
