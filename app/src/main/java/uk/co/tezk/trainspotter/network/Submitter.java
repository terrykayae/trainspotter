package uk.co.tezk.trainspotter.network;

import android.util.Log;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.model.ApiMessage;
import uk.co.tezk.trainspotter.model.SightingDetails;

import static uk.co.tezk.trainspotter.model.Constant.API_KEY;
import static uk.co.tezk.trainspotter.model.Constant.SUCCESS_MESSAGE;

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

    public void submitSighting(final SightingDetails sightingDetails) {
        Observable<ApiMessage> s = trainSpottingApi.addTrainSighting(
                sightingDetails.getTrainClass(),
                sightingDetails.getTrainId(),
                sightingDetails.getDate(),
                sightingDetails.getLat(),
                sightingDetails.getLon(),
                API_KEY);
        s.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ApiMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ApiMessage s) {
                        Log.i("Submitter", "s = " + s.getMessage());
                        if (SUCCESS_MESSAGE.equals(s.getMessage())) {
                            // TODO : this should throw an error as it's actually now a RealmObject...
                            sightingDetails.setReportedToApi(true);
                        }
                    }
                });
    }
}
