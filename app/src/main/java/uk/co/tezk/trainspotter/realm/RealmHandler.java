package uk.co.tezk.trainspotter.realm;

import android.util.Log;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ImageDetails;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainListItem;

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
        if (sightingDetails.getTrainClass() == null || sightingDetails.getTrainClass().length() == 0 ||
                "0".equals(sightingDetails.getTrainClass())) {
            Log.i("RH", "sightingdetails.trainclass == null");
            RealmResults<TrainListItem> results = Realm.getDefaultInstance()
                    .where(TrainListItem.class)
                    .equalTo("number", sightingDetails.getTrainId())
                    .findAll();
            Log.i("RH", "results = "+results);
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
        Realm instance = Realm.getDefaultInstance();
        Log.i("RH", "Executing transaction");
        instance.executeTransaction(new Realm.Transaction()

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


        Log.i("RH", "Saved, currently logged sightings : " + Realm.getDefaultInstance().where(SightingDetails.class).count());
        return sightingDetails;
    }

    public RealmResults<SightingDetails> getSightings(String classNum, String trainNum) {
        //TODO : get on classNum as well
        Realm realm = Realm.getDefaultInstance();
        RealmResults<SightingDetails> results = realm.where(SightingDetails.class).equalTo("trainId", trainNum).findAll();
        return results;
    }

    public void persistImageDetails(final List<String> imageFilenames, final SightingDetails sightingDetails) {
        Log.i("REALM","saveImage = "+imageFilenames.size());
        if (imageFilenames != null && imageFilenames.size() > 0) {
            final Realm instance = Realm.getDefaultInstance();
            instance.executeTransaction(new Realm.Transaction() {
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

        }
    }

    public RealmResults<ImageDetails> getImageDetails(String classNum, String trainNum) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(ImageDetails.class).equalTo("trainId", trainNum).findAll();
    }

    public RealmResults<ClassDetails> getClassList() {
        return Realm.getDefaultInstance().where(ClassDetails.class).findAll();
    }
}