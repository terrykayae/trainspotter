package uk.co.tezk.trainspotter.interactor;

import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;

/**
 * Created by tezk on 10/05/17.
 */

public class TrainSpotterInteractorImpl implements ITrainSpotterInteractor {

    ITrainSpottingRetrofit retrofit;

    public TrainSpotterInteractorImpl(ITrainSpottingRetrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public Observable<ClassNumbers> getClassNumbers() {
        return retrofit.getClassNumbers();
    }

    @Override
    public Observable<List<TrainListItem>> getTrains(String classNumber) {
        return retrofit.getTrains(classNumber);
    }

    @Override
    public Observable<TrainDetail> getTrainDetails(String classId, String trainId) {
        return retrofit.getTrainDetails(classId, trainId);

    }

    @Override
    public void addTrainSighting(SightingDetails sightingDetails, String apiKey) {
        retrofit.addTrainSighting(
                sightingDetails.getTrainClass(),
                sightingDetails.getTrainId(),
                sightingDetails.getDate(),
                sightingDetails.getLat(),
                sightingDetails.getLon(),
                apiKey
        );
    }
}
