package uk.co.tezk.trainspotter.realm;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
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

    public void persistSightingDetails(final SightingDetails sightingDetails) {
        Thread t = new Thread() {
            // Send to background thread
            @Override
            public void run() {
                // If we don't know train class, set to 0
                if (sightingDetails.getTrainClass()==null)
                    sightingDetails.setTrainClass("0");
                Realm instance = Realm.getDefaultInstance();
                instance.executeTransaction(new Realm.Transaction()

                {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm(sightingDetails);
                    }
                });

            }

        };
        t.start();
        //TODO : take out the join when we know it works
        try {
            t.join();
            Log.i("RH", "Saved, currently logged sightings : "+Realm.getDefaultInstance().where(SightingDetails.class).count());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public RealmResults<SightingDetails> getSightings(String classNum, String trainNum) {
        //TODO : get on classNum as well
        Realm realm = Realm.getDefaultInstance();
        RealmResults<SightingDetails> results = realm.where(SightingDetails.class).equalTo("trainId", trainNum).findAll();
        return results;
    }
}