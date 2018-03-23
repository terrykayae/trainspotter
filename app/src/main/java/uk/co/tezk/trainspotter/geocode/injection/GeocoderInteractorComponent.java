package uk.co.tezk.trainspotter.geocode.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.injection.NetworkComponent;
import uk.co.tezk.trainspotter.geocode.GeocodePresenter;

/**
 * Created by tezk on 23/05/17.
 */
@Component(modules = GeocoderInteractorModule.class ,dependencies = NetworkComponent.class)
public interface GeocoderInteractorComponent {

        void inject(GeocodePresenter geocodePresenter) ;

}
