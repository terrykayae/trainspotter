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
import uk.co.tezk.trainspotter.presenter.ClassListPresenterImpl;
import uk.co.tezk.trainspotter.presenter.IClassListPresenter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tezk on 11/05/17.
 */

public class ClassListPresenterTest {
    // The presenter under test
    IClassListPresenter.IPresenter presenter;
    // The mocked items
    @Mock
    IClassListPresenter.IView view;
    @Mock
    ITrainSpotterInteractor interactor;
    // Test data
    List <String> classList;
    ClassNumbers classNumbers;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        presenter = new ClassListPresenterImpl(interactor, Schedulers.immediate(), Schedulers.immediate());
        classList = new ArrayList();
        classNumbers = new ClassNumbers();
        classList.add("1");
        classList.add("2");
        classList.add("3");
        classNumbers.setClassNumbers(new ArrayList());
        classNumbers.getClassNumbers().addAll(classList);
    }

    @Test
    public void testBindingAndErrorPassingWorks() {
        when(interactor.getClassNumbers()).thenReturn(Observable.<ClassNumbers>error(new Exception("No data returned from the server")));
        presenter.bind(view);

        presenter.retrieveData();

        verify(view, times(1)).onStartLoading();
        verify(view, times(1)).onErrorLoading("No data returned from the server");
    }
    
    @Test
    public void testBindingAndGetDataWorks() {
        when(interactor.getClassNumbers()).thenReturn(Observable.just(classNumbers));
        presenter.bind(view);

        presenter.retrieveData();

        verify(view, times(1)).showClassList(classList);
        verify(view, times(1)).onCompletedLoading();
    }
    
    
}
