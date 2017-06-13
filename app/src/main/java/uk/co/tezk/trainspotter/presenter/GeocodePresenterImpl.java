package uk.co.tezk.trainspotter.presenter;

import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.interactor.IGeocoderInteractor;
import uk.co.tezk.trainspotter.model.geocoder.Geocoder;
import uk.co.tezk.trainspotter.model.geocoder.Result;

/**
 * Created by tezk on 23/05/17.
 */

public class GeocodePresenterImpl implements IGeocodePresenter.IPresenter {
    IGeocodePresenter.IView view;

    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    private CompositeSubscription compositeSubscription;

    @Inject
    IGeocoderInteractor interactor;

    public GeocodePresenterImpl() {
       TrainSpotterApplication.getApplication().getGeocoderInteractorComponent().inject(this);
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    @Override
    public void bind(IGeocodePresenter.IView view) {
        this.view = view;
        if (compositeSubscription == null || compositeSubscription.isUnsubscribed())
            compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void unBind() {
        this.view = null;
        compositeSubscription = null;
    }

    @Override
    public void getLocation(float lat, float lng, String apiKey) {
        interactor.getGeocoded(lat, lng, apiKey)
                .observeOn(observeScheduler)
                .subscribeOn(subscribeScheduler)
                .subscribe(new Observer<Geocoder>() {
                    @Override
                    public void onCompleted() {
                        // No need to pass this on
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Fail silently
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Geocoder geocoder) {
                        List<Result> results = geocoder.getResults();
                        if (results!=null && results.size()>0) {
                            String location = results.get(0).getComponents().getSuburb();
                            if (location == null || location.length()==0)
                                location = results.get(0).getComponents().getCity();
                            view.updateLocation(location);
                        }
                    }
                });

    }
}
