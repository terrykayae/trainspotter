package uk.co.tezk.trainspotter.interactor;

import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.TrainSpottingRetrofit;

import static uk.co.tezk.trainspotter.utility.Utilitity.isNetworkAvailable;

/**
 * Interactor to deal with the API - stores the Class list and any Train lists to the local Realm database
 * to allow caching
 */

public class TrainSpotterInteractorImpl implements TrainSpotterInteractor {

    TrainSpottingRetrofit retrofit;

    // Default constructor
     private TrainSpotterInteractorImpl() {
        //TrainSpotterApplication.getApplication().getNetworkComponent().inject(this);
    }

    public TrainSpotterInteractorImpl(TrainSpottingRetrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public Observable<ClassNumbers> getClassNumbers() {
        if (!isNetworkAvailable())
            return Observable.empty();
        // Fetch then cache the numbers
        Observable<ClassNumbers> classNumbers = retrofit.getClassNumbers();
       // if (!mockNetworkTest)
       //     CachedInteractor.getInstance().cacheClassList(classNumbers);
       // CachedInteractor.getInstance().unsubscribe();
        return classNumbers;
    }

    @Override
    public Observable<List<TrainListItem>> getTrains(String classNumber) {
        if (!isNetworkAvailable())
            return Observable.empty();
        Observable<List<TrainListItem>> trains = retrofit.getTrains(classNumber);
     //   if (!mockNetworkTest)
     //       CachedInteractor.getInstance().cacheTrainList(trains);
      //  CachedInteractor.getInstance().unsubscribe();
        return trains;
    }

    @Override
    public Observable<TrainDetail> getTrainDetails(String classId, String trainId) {
        if (!isNetworkAvailable())
            return Observable.empty();
        return retrofit.getTrainDetails(classId, trainId);
    }

    @Override
    public void addTrainSighting(SightingDetails sightingDetails, String apiKey) {
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
