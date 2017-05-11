import android.app.Application;

import uk.co.tezk.trainspotter.network.DaggerNetworkComponent;
import uk.co.tezk.trainspotter.network.NetworkComponent;

/**
 * Created by tezk on 11/05/17.
 */

public class TrainSpotterApplication extends Application {
    Application application;
    NetworkComponent networkComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        networkComponent = DaggerNetworkComponent.create();
    }

    public Application getApplication() {
        return application;
    }

    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }
}
