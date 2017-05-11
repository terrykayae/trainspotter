package uk.co.tezk.trainspotter.interactor;

import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Interface for the interactor - encapsulates the use cases to get Observables that fit our needs
 */

public interface ITrainSpotterInteractor {
    public Observable<ClassNumbers> getClassNumbers() ;
    public Observable<List<TrainListItem>> getTrains(String classNumber) ;
    public Observable<TrainDetail> getTrainDetails(String trainId, String classNumber) ;

    public void addTrainSighting(SightingDetails sightingDetails, String apiKey) ;

}
