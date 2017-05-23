package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.presenter.GeocodePresenterImpl;

/**
 * Created by tezk on 23/05/17.
 */
@Component(modules = GeocoderInteractorModule.class ,dependencies = NetworkComponent.class)
public interface GeocoderInteractorComponent {

        void inject(GeocodePresenterImpl geocodePresenter) ;

}
