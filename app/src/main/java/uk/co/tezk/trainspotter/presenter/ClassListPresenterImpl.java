package uk.co.tezk.trainspotter.presenter;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.network.NetworkModule;

/**
 * Created by tezk on 11/05/17.
 */

public class ClassListPresenterImpl implements IClassListPresenter.IPresenter {
    private static ClassListPresenterImpl presenter;


    IClassListPresenter.IView view;
    ITrainSpotterInteractor interactor;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    public static ClassListPresenterImpl getInstance() {
        if (presenter == null) {
            NetworkModule net = new NetworkModule();
            presenter = new ClassListPresenterImpl(new TrainSpotterInteractorImpl(new NetworkModule().provideApi(net.provideRetrofit(net.provideOkHttpclient(net.provideInterceptor())))));
        }
        return presenter;
    }

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
