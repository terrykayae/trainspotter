package uk.co.tezk.trainspotter;

import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import rx.Observable;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;
import uk.co.tezk.trainspotter.network.NetworkModule;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by tezk on 11/05/17.
 */

public class ApiUnitTest {
    @Inject
    ITrainSpottingRetrofit api;

    @Before
    public void setUp() throws Exception {
        NetworkModule net = new NetworkModule();
        api = new NetworkModule().provideApi(net.provideRetrofit(net.provideOkHttpclient(net.provideInterceptor())));
    }

    @Test
    public void testGetClasses() {
        Observable<ClassNumbers> classNumbers = api.getClassNumbers();
        assertNotNull(classNumbers);
    }
}
