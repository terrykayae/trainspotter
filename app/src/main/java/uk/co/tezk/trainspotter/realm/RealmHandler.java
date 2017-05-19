package uk.co.tezk.trainspotter.realm;

import android.util.Log;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.model.ImageDetails;
import uk.co.tezk.trainspotter.model.SightingDetails;

/**
 * Created by tezk on 15/05/17.
 */

public class RealmHandler {
    private static RealmHandler realmHandler;

    private RealmHandler() {

    }

    public static RealmHandler getInstance() {
        if (realmHandler == null) {
            realmHandler = new RealmHandler();
        }
        return realmHandler;
    }

    public SightingDetails persistSightingDetails(final SightingDetails sightingDetails) {
        SightingDetails toRealm;
                // If we don't know train class, set to 0
                if (sightingDetails.getTrainClass()==null)
                    sightingDetails.setTrainClass("0");
                Realm instance = Realm.getDefaultInstance();
                instance.executeTransaction(new Realm.Transaction()

                {
                    @Override
                    public void execute(Realm realm) {
                        SightingDetails toRealm = realm.copyToRealm(sightingDetails);
                    }
                });


            Log.i("RH", "Saved, currently logged sightings : "+Realm.getDefaultInstance().where(SightingDetails.class).count());
            return sightingDetails;
    }

    public RealmResults<SightingDetails> getSightings(String classNum, String trainNum) {
        //TODO : get on classNum as well
        Realm realm = Realm.getDefaultInstance();
        RealmResults<SightingDetails> results = realm.where(SightingDetails.class).equalTo("trainId", trainNum).findAll();
        return results;
    }

    public void persistImageDetails(final List<String> imageFilenames, final SightingDetails sightingDetails) {
        if (imageFilenames!=null && imageFilenames.size()>0) {
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
                        realm.copyToRealmOrUpdate(imageDetails);
                    }
                }
            });

        }
    }

    public RealmResults <ImageDetails>getImageDetails(String classNum, String trainNum) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(ImageDetails.class).equalTo("trainId", trainNum).findAll();
    }
}