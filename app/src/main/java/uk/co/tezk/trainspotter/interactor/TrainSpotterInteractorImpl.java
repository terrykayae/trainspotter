package uk.co.tezk.trainspotter.interactor;

import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;
import uk.co.tezk.trainspotter.realm.ApiCache;

/**
 * Interactor to deal with the API - stores the Class list and any Train lists to the local Realm database
 * to allow caching
 */

public class TrainSpotterInteractorImpl implements ITrainSpotterInteractor {

    ITrainSpottingRetrofit retrofit;

    // Default constructor
     private TrainSpotterInteractorImpl() {
        //TrainSpotterApplication.getApplication().getNetworkComponent().inject(this);
    }

    public TrainSpotterInteractorImpl(ITrainSpottingRetrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public Observable<ClassNumbers> getClassNumbers() {
        // Fetch then cache the numbers
        Observable<ClassNumbers> classNumbers = retrofit.getClassNumbers();
        ApiCache.getInstance().cacheClassList(classNumbers);
        return classNumbers;
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
