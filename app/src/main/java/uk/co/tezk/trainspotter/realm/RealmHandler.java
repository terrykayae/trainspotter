package uk.co.tezk.trainspotter.realm;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ImageDetails;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.utility.Utilitity;

import static android.app.backup.BackupManager.dataChanged;

/**
 * Deal with Realm interactions
 */

public class RealmHandler {
    private static RealmHandler realmHandler;

    // Implement singleton pattern
    private RealmHandler() {
    }

    public static RealmHandler getInstance() {
        if (realmHandler == null) {
            realmHandler = new RealmHandler();
        }
        return realmHandler;
    }

    // Save sightings
    public SightingDetails persistSightingDetails(final SightingDetails sightingDetails) {
        // If we don't know train class, try and find, or set to 0
        Realm realm = Realm.getDefaultInstance();
        if (sightingDetails.getTrainClass() == null || sightingDetails.getTrainClass().length() == 0 ||
                "0".equals(sightingDetails.getTrainClass())) {
            RealmResults<TrainListItem> results = realm.where(TrainListItem.class)
                    .equalTo("number", sightingDetails.getTrainId())
                    .findAll();
            if (results.size()>0) {
                Log.i("RH", "class is "+results.get(0).getClass_());
            } else {
                Log.i("RH", "none found?");
            }
            if (results.size() == 1) {
                sightingDetails.setTrainClass(results.get(0).getClass_());
            } else {
                sightingDetails.setTrainClass("0");
            }
        }

        Log.i("RH", "Executing transaction");
        realm.executeTransaction(new Realm.Transaction()

        {
            @Override
            public void execute(Realm realm) {
              /*  long currentTrainSightings = realm.where(SightingDetails.class)
                        .equalTo("trainId", sightingDetails.getTrainId())
                        .equalTo("trainClass", sightingDetails.getTrainClass())
                        .count();*/

                long trainsInClassSpotted = realm.where(SightingDetails.class)
                        .equalTo("trainClass", sightingDetails.getTrainClass())
                        .distinct("trainId")
                        .size();
                Log.i("RH", "trains in class spotted = "+trainsInClassSpotted);
                Log.i("RH", "class = "+realm.where(ClassDetails.class)
                        .equalTo("classId", sightingDetails.getTrainClass())
                        .findFirst());

                ClassDetails classId = realm.where(ClassDetails.class)
                        .equalTo("classId", "" + sightingDetails.getTrainClass())
                        .findFirst();
                if (classId!=null)
                        classId.setSightingsRecorded((int) trainsInClassSpotted);

                SightingDetails toRealm = realm.copyToRealm(sightingDetails);
            }
        });

        realm.close();
        Log.i("RH", "Saved, currently logged sightings : " + Realm.getDefaultInstance().where(SightingDetails.class).count());
        // inform our DataBackupHelper (RealmCloudBackup) data has changed
        dataChanged(TrainSpotterApplication.getApplication().getPackageName());
        return sightingDetails;
    }

    public RealmResults<SightingDetails> getSightings(String classNum, String trainNum) {
        //TODO : get on classNum as well
        Realm realm = Realm.getDefaultInstance();
        RealmResults<SightingDetails> results = realm.where(SightingDetails.class).equalTo("trainId", trainNum).findAll();
        realm.close();
        return results;
    }

    public void persistImageDetails(final List<String> imageFilenames, final SightingDetails sightingDetails) {
        Log.i("REALM","saveImage = "+imageFilenames.size());
        if (imageFilenames != null && imageFilenames.size() > 0) {
            final Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (String each : imageFilenames) {
                        ImageDetails imageDetails = new ImageDetails();
                        imageDetails.setImageUrl(each);
                        imageDetails.setDate(sightingDetails.getDate());
                        imageDetails.setLocationName(sightingDetails.getLocationName());
                        imageDetails.setClassNum(sightingDetails.getTrainClass());
                        imageDetails.setTakenByUs(true);
                        imageDetails.setTrainNum(sightingDetails.getTrainId());
                        imageDetails.setTime(Utilitity.getTime(new Date()));
                        realm.copyToRealm(imageDetails);
                    }
                }
            });
            realm.close();
            // inform our DataBackupHelper (RealmCloudBackup) data has changed
            dataChanged(TrainSpotterApplication.getApplication().getPackageName());
        }
    }

    public RealmResults<ImageDetails> getImageDetails(String classNum, String trainNum) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImageDetails> imageDetails = realm.where(ImageDetails.class).equalTo("trainNum", trainNum).findAll();
        realm.close();
        return imageDetails;
    }

    public RealmResults<ClassDetails> getClassList() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ClassDetails> results = realm.where(ClassDetails.class).findAll();
        return results;
    }

    public List<TrainDetail> performSearch(String searchString) {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<TrainListItem> results = realm.where(TrainListItem.class)
                .contains("name", searchString, Case.INSENSITIVE)
                .or()
                .contains("number", searchString, Case.INSENSITIVE)
                .findAll();

        List <TrainDetail> newTrainList = new ArrayList<>();

        if (results != null) {
            for (TrainListItem each : results) {
                TrainDetail trainDetail = new TrainDetail();
                trainDetail.setTrain(each);
                trainDetail.setSightings(getSightings(each.getClass_(), each.getNumber()));
                trainDetail.setImages(realmResultsToList(getImageDetails(each.getClass_(), each.getNumber())));

                newTrainList.add(trainDetail);
            }
        }

        realm.close();

        return newTrainList;
    }

    public List realmResultsToList(RealmResults results) {
        List newList = new ArrayList();
        newList.addAll(results);
        return newList;
    }
}