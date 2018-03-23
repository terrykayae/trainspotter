package uk.co.tezk.trainspotter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.presenter.TrainDetailContract;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenterImpl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tezk on 12/05/17.
 */

public class TrainDetailPresenterTest {
    // The presenter under test
    TrainDetailContract.Presenter presenter;
    // The mocked items
    @Mock
    TrainDetailContract.View view;
    @Mock
    TrainSpotterInteractor interactor;
    // Test data
    private TrainListItem trainListItem;
    private TrainDetail trainDetail;
    private List <SightingDetails> sightingDetails;
    private static final String CLASS_ID = "1";
    private static final String ENGINE_ID = "1000";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        presenter = new TrainDetailPresenterImpl(interactor, Schedulers.immediate(), Schedulers.immediate());
        trainDetail = new TrainDetail();
        trainDetail.setTrain(new TrainListItem(CLASS_ID, "1000", "Thomas", "", "", "", ""));
        sightingDetails = new ArrayList();
        sightingDetails.add(new SightingDetails("Today", 52f, 0f));
        sightingDetails.add(new SightingDetails("Tomorrow", 52f, 0f));
        trainDetail.setSightings(sightingDetails);
        trainDetail.setImages(new ArrayList<String>());
    }

    @Test
    public void testBindingAndErrorPassingWorks() {
        when(interactor.getTrainDetails(CLASS_ID, ENGINE_ID)).thenReturn(Observable.<TrainDetail>error(new Exception("No data returned from the server")));
        presenter.bind(view);

        presenter.retrieveData(CLASS_ID, ENGINE_ID);

        verify(view, times(1)).onStartLoading();
        verify(view, times(1)).onErrorLoading("No data returned from the server");
    }

    @Test
    public void testBindingAndGetDataWorks() {
        when(interactor.getTrainDetails(CLASS_ID, ENGINE_ID)).thenReturn(Observable.just(trainDetail));
        presenter.bind(view);

        presenter.retrieveData(CLASS_ID, ENGINE_ID);

        verify(view, times(2)).showTrainDetails(trainDetail);
        verify(view, times(1)).onCompletedLoading();
    }
}
