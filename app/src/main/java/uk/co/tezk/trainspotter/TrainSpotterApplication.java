package uk.co.tezk.trainspotter;

import android.app.Application;
import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import uk.co.tezk.trainspotter.interactor.DaggerTrainSpotterInteractorComponent;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorComponent;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorModule;
import uk.co.tezk.trainspotter.network.DaggerNetworkComponent;
import uk.co.tezk.trainspotter.network.NetworkComponent;

//import uk.co.tezk.trainspotter.network.DaggerNetworkComponent;

/**
 * Created by tezk on 11/05/17.
 */

public class TrainSpotterApplication extends Application {
    // Dagger things, access to Application to inject, plust injectable components
    static TrainSpotterApplication application;
    NetworkComponent networkComponent;
    TrainSpotterInteractorComponent trainSpotterInteractorComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        // Save the application reference for use in the Dagger injection
        application = this;
        // Initiate the Dagger componenets
        networkComponent = DaggerNetworkComponent.create();
        trainSpotterInteractorComponent = DaggerTrainSpotterInteractorComponent.builder()
                .networkComponent(networkComponent)
                .trainSpotterInteractorModule(new TrainSpotterInteractorModule())
                .build();
        // Initialise Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().
                schemaVersion(1).
                migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();
                        Log.i("myapp", "Migration running : old = " + oldVersion + ", new = " + newVersion);
                        if (oldVersion == 1) {
                            //    schema.get("MyRealmObject").removeField(("String3"));
                            //    schema.get("MyRealmObject").addField("string3", String.class);
                            //    Log.i("myapp","schema updated");
                        }
                        return;
                    }
                }).build();
        Realm.setDefaultConfiguration(config);
    }

    public static TrainSpotterApplication getApplication() {
        return application;
    }

    public NetworkComponent getNetworkComponent() {
       return networkComponent;
    }

    public TrainSpotterInteractorComponent getTrainSpotterInteractorComponent() {
        return trainSpotterInteractorComponent;
    }
}
