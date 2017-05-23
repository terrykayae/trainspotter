package uk.co.tezk.trainspotter.presenter;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.realm.ApiCache;
import uk.co.tezk.trainspotter.realm.RealmHandler;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 * and then transforming these into ClassDetails items
 */

public class ClassListPresenterImpl implements
        IClassListPresenter.IPresenter,
        IClassListApiPresenter.IView,
        ApiCache.ApiBinder {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    IClassListPresenter.IView view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    IClassListApiPresenter.IPresenter presenter;

    public ClassListPresenterImpl() {
        observeScheduler = AndroidSchedulers.mainThread();
        subscribeScheduler = Schedulers.io();
    }

    @Override
    public void retrieveData() {
        view.onStartLoading();
        RealmResults<ClassDetails> results = RealmHandler.getInstance().getClassList().sort("classId");
        if (results.size() == 0) {
            // We need to get from the API and build the DB
            presenter = new ClassListApiPresenterImpl();
            presenter.bind(this);
            presenter.retrieveData();
        } else {
            view.showClassList(results);
            view.onCompletedLoading();
        }

    }


    @Override
    public void bind(IClassListPresenter.IView view) {
                    this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        if (compositeSubscription!=null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }

    // From the API presenter
    @Override
    public void showClassList(List<String> classList) {
        // Now we've got the class list (list of Strings) create the ClassDetails and pass those back
        ApiCache apiCache = new ApiCache();
        apiCache.bind(this);
        ClassNumbers classNumbers = new ClassNumbers();
        classNumbers.setClassNumbers(classList);
        apiCache.cacheClassList(Observable.just(classNumbers));
        // Now inform the View we've got the initial list from the API
        List <ClassDetails> newClassList = new ArrayList();
        for (String each : classList) {
            ClassDetails classDetails = new ClassDetails();
            classDetails.setClassId(each);
            newClassList.add(classDetails);
        }

        view.showClassList(newClassList);
        view.onCompletedLoading();
    }

    @Override
    public void onStartLoading() {
        // Already passed
    }

    @Override
    public void onErrorLoading(String message) {
        view.onErrorLoading(message);
    }

    @Override
    public void onCompletedLoading() {
       // Not done yet, we've called ApiCache.
    }


    // Call back when the ApiCache is completed saving
    @Override
    public void onApiCompleted() {
        view.onCompletedLoading();
    }
}
