package uk.co.tezk.trainspotter.classList;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.realm.CachedInteractor;
import uk.co.tezk.trainspotter.realm.RealmHandler;

/**
 * Implementation for the ClassList presenter - responsible for fetching and returning the class list from the API
 * and then transforming these into ClassDetails items
 */

public class ClassListPresenter implements
        ClassListContract.Presenter,
        ClassListApiContract.View,
        CachedInteractor.CacheListener {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    ClassListContract.View view;
    Scheduler observeScheduler;
    Scheduler subscribeScheduler;

    ClassListApiContract.Presenter presenter;

    private boolean skipRealm;

    public ClassListPresenter(ClassListApiContract.Presenter presenter,
                                  boolean skipRealm) {
        this.presenter = presenter;
        this.skipRealm = skipRealm;
    }

    @Override
    public void retrieveData() {
        view.onStartLoading();
        RealmResults<ClassDetails> results = null;
        if (!skipRealm) {
             results = RealmHandler.getInstance().getClassList().sort("classId");
        }
        if (skipRealm || results.size() == 0) {
            // We need to get from the API and build the DB
            if (presenter == null) {
               presenter = new ClassListApiPresenter();
            }
            presenter.bind(this);
            presenter.retrieveData();
        } else {
            view.showClassList(results);
            view.onCompletedLoading();
        }
    }

    @Override
    public void bind(ClassListContract.View view) {
                    this.view = view;
    }

    @Override
    public void unbind() {
        view = null;
        compositeSubscription.clear();
    }

    // From the API presenter
    @Override
    public void showClassList(List<String> classList) {
        // Now we've got the class list (list of Strings) create the ClassDetails and pass those back
        CachedInteractor cachedInteractor = null;
        if (!skipRealm) {
            cachedInteractor = new CachedInteractor();
            cachedInteractor.attachListener(this);
        }
        ClassNumbers classNumbers = new ClassNumbers();
        classNumbers.setClassNumbers(classList);
        if (!skipRealm) {
            cachedInteractor.cacheClassList(Observable.just(classNumbers));
        }
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
       // Not done yet, we've called CachedInteractor.
    }

    // Call back when the CachedInteractor is completed saving
    @Override
    public void onApiCompleted() {
        view.onCompletedLoading();
    }
}
