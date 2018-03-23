package uk.co.tezk.trainspotter.injection;

import dagger.Component;
import uk.co.tezk.trainspotter.classList.ClassListFragment;
import uk.co.tezk.trainspotter.view.TrainDetailFragment;
import uk.co.tezk.trainspotter.view.TrainListFragment;

@Component(modules = PresentersModule.class, dependencies = TrainSpotterInteractorComponent.class )
public interface PresenterComponent {
    void inject(ClassListFragment classListFragment) ;
    void inject(TrainListFragment trainListFragment) ;
    void inject(TrainDetailFragment trainDetailFragment) ;
}
