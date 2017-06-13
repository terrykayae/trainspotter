package uk.co.tezk.trainspotter.interactor;

import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;
import uk.co.tezk.trainspotter.realm.ApiCache;

import static uk.co.tezk.trainspotter.utility.Utilitity.isNetworkAvailable;

/**
 * Interactor to deal with the API - stores the Class list and any Train lists to the local Realm database
 * to allow caching
 */

public class TrainSpotterInteractorImpl implements ITrainSpotterInteractor {

    ITrainSpottingRetrofit retrofit;

    // for testing
    boolean mockNetworkTest = false;

    // Default constructor
     private TrainSpotterInteractorImpl() {
        //TrainSpotterApplication.getApplication().getNetworkComponent().inject(this);
    }

    public TrainSpotterInteractorImpl(ITrainSpottingRetrofit retrofit, boolean passNetworkTest) {
        this.retrofit = retrofit;
        this.mockNetworkTest = passNetworkTest;
    }

    @Override
    public Observable<ClassNumbers> getClassNumbers() {
        if (!mockNetworkTest && !isNetworkAvailable())
            return Observable.empty();
        // Fetch then cache the numbers
        Observable<ClassNumbers> classNumbers = retrofit.getClassNumbers();
        if (!mockNetworkTest)
            ApiCache.getInstance().cacheClassList(classNumbers);
       // ApiCache.getInstance().unsubscribe();
        return classNumbers;
    }

    @Override
    public Observable<List<TrainListItem>> getTrains(String classNumber) {
        if (!mockNetworkTest && !isNetworkAvailable())
            return Observable.empty();
        Observable<List<TrainListItem>> trains = retrofit.getTrains(classNumber);
        if (!mockNetworkTest)
            ApiCache.getInstance().cacheTrainList(trains);
      //  ApiCache.getInstance().unsubscribe();
        return trains;
    }

    @Override
    public Observable<TrainDetail> getTrainDetails(String classId, String trainId) {
        if (!mockNetworkTest && !isNetworkAvailable())
            return Observable.empty();
        return retrofit.getTrainDetails(classId, trainId);
    }

    @Override
    public void addTrainSighting(SightingDetails sightingDetails, String apiKey) {
        if (!mockNetworkTest)
            if (!isNetworkAvailable())
                return;
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
