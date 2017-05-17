package uk.co.tezk.trainspotter.injection;

import dagger.Module;
import dagger.Provides;
import uk.co.tezk.trainspotter.model.MyLocation;

/**
 * Created by tezk on 23/04/17.
 */
@Module
public class LocationModule {
    @Provides
    MyLocation getMyLocation() {

        return new MyLocation();
    }

}
