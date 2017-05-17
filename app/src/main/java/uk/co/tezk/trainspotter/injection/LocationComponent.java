package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.view.LogSpotFragment;

/**
 * Created by tezk on 16/05/17.
 */
@Component(modules = LocationModule.class)
public interface LocationComponent {
    void inject(LogSpotFragment logSpotFragment);
}
