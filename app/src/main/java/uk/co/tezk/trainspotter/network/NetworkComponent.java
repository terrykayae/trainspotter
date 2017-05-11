package uk.co.tezk.trainspotter.network;

import dagger.Component;
import uk.co.tezk.trainspotter.MainActivity;

/**
 * Create the injections
 */
@Component(modules = NetworkModule.class)
public interface NetworkComponent {
    void inject(MainActivity mainActivity);
}
