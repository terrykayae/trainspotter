package uk.co.tezk.trainspotter.network;

import android.util.Log;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.model.SightingDetails;

import static uk.co.tezk.trainspotter.model.Constant.API_KEY;

/**
 * Class to deal with sending data to the API server
 */

public class Submitter {
    private static Submitter submitter;
    @Inject
    static ITrainSpottingRetrofit trainSpottingApi;

    private Submitter() {
        TrainSpotterApplication.getApplication().getNetworkComponent().inject(this);
    }

    public static Submitter getInstance() {
        if (submitter == null) {
            submitter = new Submitter();
        }
        return submitter;
    }

    public void submitSighting(SightingDetails sightingDetails) {
        Observable<String> s = trainSpottingApi.addTrainSighting(
                sightingDetails.getTrainClass(),
                sightingDetails.getTrainId(),
                sightingDetails.getDate(),
                sightingDetails.getLat(),
                sightingDetails.getLon(),
                API_KEY);
        s.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        Log.i("Submitter", "s = " + s);
                    }
                });
    }
}
