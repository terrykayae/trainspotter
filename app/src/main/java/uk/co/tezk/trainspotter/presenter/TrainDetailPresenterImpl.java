package uk.co.tezk.trainspotter.presenter;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 12/05/17.
 */

public class TrainDetailPresenterImpl implements ITrainDetailPresenter.IPresenter {
    private static TrainDetailPresenterImpl presenter;

    ITrainDetailPresenter.IView view;
    @Inject
    ITrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    // Various constructors to aid in testing
    public TrainDetailPresenterImpl() {
        // Dagger inject for interactor
        TrainSpotterApplication.getApplication().getTrainSpotterInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

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
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }

    @Override
    public void retrieveData(String classNum, String engineNum) {
        view.onStartLoading();
        compositeSubscription.add(interactor.getTrainDetails(classNum, engineNum)
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
    }
}
