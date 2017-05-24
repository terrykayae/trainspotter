package uk.co.tezk.trainspotter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import uk.co.tezk.trainspotter.interactor.ITrainSpotterInteractor;
import uk.co.tezk.trainspotter.interactor.TrainSpotterInteractorImpl;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.tezk.trainspotter.model.Constant.API_KEY;

/**
 * Created by tezk on 10/05/17.
 */

public class InteractorUnitTest {
    @Mock Observable<ClassNumbers> classTestResult;
    @Mock Observable<List<TrainListItem>> trainTestResult;
    @Mock Observable<TrainDetail> trainDetailResult;
    @Mock ITrainSpottingRetrofit retrofit;

    ClassNumbers listOfClasses;
    List<TrainListItem> trainList;
    TrainDetail trainDetail;

    ITrainSpotterInteractor interactor;

    @Before
    public void setUp() throws Exception {
        // Initialise our mocks
        MockitoAnnotations.initMocks(this);
        interactor = new TrainSpotterInteractorImpl(retrofit, true);
        listOfClasses = new ClassNumbers();
        listOfClasses.setClassNumbers(new ArrayList<String>());
        listOfClasses.getClassNumbers().add("1");
        listOfClasses.getClassNumbers().add("2");
        listOfClasses.getClassNumbers().add("3");
        classTestResult = Observable.just(listOfClasses);

        trainList = new ArrayList();
        trainList.add(new TrainListItem("1","1000","Thomas", "", "", "", ""));
        trainList.add(new TrainListItem("1","1001","Percy", "", "", "", ""));
        trainList.add(new TrainListItem("1","1002","Edward", "", "", "", ""));
        trainTestResult = Observable.just(trainList);

        trainDetail = new TrainDetail();
        trainDetail.setTrain(trainList.get(0));
        SightingDetails sighting = new SightingDetails("1/1/17", 52, 1);
        List <SightingDetails> sightingList = new ArrayList<>();
        sightingList.add(sighting);
        trainDetail.setSightings(sightingList);
        trainDetailResult = Observable.just(trainDetail);
    }

    @Test
    public void testGetClassNumbersReturnsListOfNumbers() {
        when(retrofit.getClassNumbers()).thenReturn(classTestResult);

        Observable<ClassNumbers> classNumbers = interactor.getClassNumbers();

        assertEquals(classNumbers, classTestResult);
        verify(retrofit, times(1)).getClassNumbers();
    }

    @Test
    public void testGetTrainsReturnsListOfTrains() {
        when(retrofit.getTrains("1")).thenReturn(trainTestResult);

        Observable<List<TrainListItem>> trains = interactor.getTrains("1");

        assertEquals(trainTestResult, trains);
        verify(retrofit, times(1)).getTrains("1");
    }

    @Test
    public void testGetTrainDetails() {
        when(retrofit.getTrainDetails("1", "1000")).thenReturn(trainDetailResult);

        Observable<TrainDetail> details = interactor.getTrainDetails("1", "1000");

        assertEquals(trainDetailResult, details);
        verify(retrofit, times(1)).getTrainDetails("1", "1000");
    }

    @Test
    public void testAddSighting() {

        SightingDetails sightingDetails = new SightingDetails();
        sightingDetails.setDate("01-01-2017");
        sightingDetails.setLat(52);
        sightingDetails.setLon(0);

        sightingDetails.setTrainClass("1");
        sightingDetails.setTrainId("1001");

        interactor.addTrainSighting(sightingDetails, API_KEY);

        verify(retrofit, times(1)).addTrainSighting(
                "1",
                "1001",
                sightingDetails.getDate(),
                sightingDetails.getLat(),
                sightingDetails.getLon(),
                API_KEY
        );
    }
}
