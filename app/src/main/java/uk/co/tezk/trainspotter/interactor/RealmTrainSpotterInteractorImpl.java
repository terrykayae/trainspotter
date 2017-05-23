package uk.co.tezk.trainspotter.interactor;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.ImageDetails;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Implements Realm based interactions with data, used as cache for data pulled from API
 */

public class RealmTrainSpotterInteractorImpl implements ITrainSpotterInteractor {
    private Realm realm;

    public RealmTrainSpotterInteractorImpl() {
//        realm = Realm.getDefaultInstance();
    }

    @Override
    public Observable<ClassNumbers> getClassNumbers() {
        if (realm == null)
            realm = Realm.getDefaultInstance();

        Log.i("RTSI", "realm class Count = " + realm.where(ClassDetails.class).count());

        RealmResults<ClassDetails> results = realm.where(ClassDetails.class).findAll();
        // Nothing in database, return empty observable
        if (results.size() == 0)
            return Observable.empty();
        // Need to return an observable that is an instance of ClassNumbers with a list of Strings
        ClassNumbers classNumbers = new ClassNumbers();
        List<String> classNumberList = new ArrayList();
        classNumbers.setClassNumbers(classNumberList);
        for (ClassDetails each : results) {
            classNumberList.add(each.getClassId());
        }
        return Observable.just(classNumbers);
    }

    @Override
    public Observable<List<TrainListItem>> getTrains(String classNumber) {
        if (realm == null)
            realm = Realm.getDefaultInstance();

        Log.i("RTSI", "realm train Count = " + realm.where(TrainListItem.class).count());

        RealmResults<TrainListItem> results = realm.where(TrainListItem.class).equalTo("_class", classNumber).findAll();
        // Nothing in database, return empty observable
        Log.i("RTSI", "Searching for \"" + classNumber + "\" found " + results.size());
        if (results.size() == 0)
            return Observable.empty();
        // Need to return an Observable that has a list of Trains
        // TODO : Check if this crashes
        final List<TrainListItem> newList = new ArrayList<>();
        for (TrainListItem each : results) {
            newList.add(each);
        }


        return Observable.just(newList);
    }

    @Override
    public Observable<TrainDetail> getTrainDetails(String classNumber, String trainId) {
        realm = Realm.getDefaultInstance();

        RealmResults <TrainListItem> results = realm.where(TrainListItem.class).findAll();
        Log.i("RTSI", "Trains found:");

        TrainListItem result = realm.where(TrainListItem.class)
                .equalTo("_class", classNumber)
                .equalTo("number", trainId)
                .findFirst();
        // Nothing in database, return empty observable
        Log.i("RTSI", "Searching for \"" + classNumber + ", " + trainId + "\" found " + result);
        if (result == null)
            return Observable.empty();

        // Fetch any sightings and images!
        TrainDetail trainDetail = new TrainDetail();
        trainDetail.setTrain(result);
        trainDetail.setSightings(
                realm.where(SightingDetails.class)
                .equalTo("trainId", trainId)
                .equalTo("trainClass", classNumber)
                .findAll());
        trainDetail.setImagesFromImageDetails(
                realm.where(ImageDetails.class)
                .equalTo("trainNum", trainId)
     //           .equalTo("classNum", classNumber)
                .findAll());

        Log.i("RTSII", "images saved = "+realm.where(ImageDetails.class).count());
        Log.i("RTSII", "images saved = "+realm.where(ImageDetails.class).findAll());

        return Observable.just(trainDetail);
    }

    @Override
    public void addTrainSighting(SightingDetails sightingDetails, String apiKey) {
        // Cannot cache this
        throw new RuntimeException("Cannot implement addTrainSightint() using Realm data - use TrainSpotterInteractorImpl to pass to API");
    }
}
