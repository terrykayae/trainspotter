package uk.co.tezk.trainspotter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.presenter.ITrainListPresenter;
import uk.co.tezk.trainspotter.presenter.TrainListPresenterImpl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test suite for the train list presenter
 */

public class TrainListPresenterTest {
    // The presenter under test
    private ITrainListPresenter.IPresenter presenter;
    // The mocked items
    @Mock
    ITrainListPresenter.IView view;
    @Mock
    ITrainSpotterInteractor interactor;
    @Mock
    ITrainSpotterInteractor cachedInteractor;
    // Test data
    private List<TrainListItem> trainList;
    private List<TrainDetail> trainDetailList;
    private static final String CLASS_ID = "1";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        presenter = new TrainListPresenterImpl(interactor, Schedulers.immediate(), Schedulers.immediate(), cachedInteractor);
        trainList = new ArrayList();
        trainDetailList = new ArrayList();

        TrainListItem trainListItem;
        TrainDetail trainDetail;
        trainList.add(trainListItem = new TrainListItem(CLASS_ID, "1000", "Thomas", "", "", "", ""));
        trainDetail = new TrainDetail();
        trainDetail.setTrain(trainListItem);
        trainDetailList.add(trainDetail);
        trainList.add(trainListItem = new TrainListItem(CLASS_ID, "1001", "Henry", "", "", "", ""));
        trainDetail = new TrainDetail();
        trainDetail.setTrain(trainListItem);
        trainDetailList.add(trainDetail);
        trainList.add(trainListItem = new TrainListItem(CLASS_ID, "1002", "Percy", "", "", "", ""));
        trainDetail = new TrainDetail();
        trainDetail.setTrain(trainListItem);
        trainDetailList.add(trainDetail);
    }

    @Test
    public void testBindingAndErrorPassingWorks() {
        when(cachedInteractor.getTrains(CLASS_ID)).thenReturn(Observable.<List<TrainListItem>>error(new Exception("No data returned from the server")));
        when(interactor.getTrains(CLASS_ID)).thenReturn(Observable.<List<TrainListItem>>error(new Exception("No data returned from the server")));
        presenter.bind(view);

        presenter.retrieveData(CLASS_ID);

        verify(view, times(1)).onStartLoading();
        verify(view, times(1)).onErrorLoading("No data returned from the server");
    }

    @Test
    public void testBindingAndGetDataNoCacheWorks() {
        when(cachedInteractor.getTrains(CLASS_ID)).thenReturn(Observable.<List<TrainListItem>>empty());
        when(interactor.getTrains(CLASS_ID)).thenReturn(Observable.just(trainList));
        presenter.bind(view);

        presenter.retrieveData(CLASS_ID);

      //  verify(view, times(1)).showTrainList(trainDetailList);
      //  verify(view, times(1)).onCompletedLoading();
    }

    @Test
    public void testBindingAndGetDataWithCacheWorks() {
        when(cachedInteractor.getTrains(CLASS_ID)).thenReturn(Observable.just(trainList));
        when(cachedInteractor.getClassNumbers()).thenReturn(Observable.<ClassNumbers>empty());
        when(interactor.getTrains(CLASS_ID)).thenReturn(Observable.just(trainList));
        presenter.bind(view);

        presenter.retrieveData(CLASS_ID);

       // verify(view, times(1)).showTrainList(trainDetailList);
      //  verify(view, times(1)).onCompletedLoading();
    }
}
