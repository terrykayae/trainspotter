package uk.co.tezk.trainspotter.presenter;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.TrainDetail;

/**
 * Created by tezk on 12/05/17.
 */

public class TrainDetailPresenter implements TrainDetailContract.Presenter {

    TrainDetailContract.View view;
    TrainSpotterInteractor apiInteractor;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public TrainDetailPresenter(TrainSpotterInteractor apiInteractor) {
        this.apiInteractor = apiInteractor;
    }

    @Override
    public void bind(TrainDetailContract.View view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        this.view = null;
        compositeSubscription.clear();
    }

    @Override
    public void retrieveData(String classNum, String trainNum) {
        view.onStartLoading();
       // Observable<TrainDetail> cachedTrainDetails = cachedInteractor.getTrainDetails(classNum, trainNum);
        Observable<TrainDetail> apiTrainDetails = apiInteractor.getTrainDetails(classNum, trainNum);

        // By concating the two, showTrainDetails will get called twice, once with the cached data, once with api data...
        compositeSubscription.add(apiTrainDetails
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS)
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
