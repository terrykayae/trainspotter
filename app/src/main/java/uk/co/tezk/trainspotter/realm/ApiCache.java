package uk.co.tezk.trainspotter.realm;


import android.util.Log;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Used by the API to persist Observables to the Realm database
 */

public class ApiCache {
    private static ApiCache apiCache;
    private Realm realm;
    private Object lock = new Object();

    public static synchronized ApiCache getInstance() {
        if (apiCache == null) {
            apiCache = new ApiCache();
        }
        return apiCache;
    }

    public void cacheClassList(Observable<ClassNumbers> classNumbersObservable) {
        // Fetch Realm instance each call as we're not sure what thread we'll be called from

        // TODO : this clears the Class list but is only called if the cached details aren't available. This should only be called once
  /*      Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(ClassDetails.class);
            }
        });*/
        if (realm!=null) {
            // Data is currently being persisted,
            do {
                synchronized (lock) {
                    Log.i("API", "Waiting for lock to be released");
                }
            } while (realm!=null);
        }

        synchronized (lock) {
            classNumbersObservable
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<ClassNumbers, Observable<String>>() {
                        @Override
                        public Observable<String> call(ClassNumbers classNumbers) {
                            return Observable.from(classNumbers.getClassNumbers());
                        }
                    })
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {

                            realm = null;
                            lock.notifyAll();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i("ApiCache", "error " + e.getMessage());
                            realm = null;
                          //  lock.notifyAll();
                        }

                        @Override
                        public void onNext(String classNumber) {
                            if (realm == null)
                                realm = Realm.getDefaultInstance();
                            final ClassDetails classDetails = new ClassDetails();
                            classDetails.setClassId(classNumber);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealm(classDetails);
                                }
                            });
                        }
                    });
        }
    }

    public void cacheTrainList(Observable <List<TrainListItem>> trainsObservable) {
        // Fetch Realm instance each call as we're not sure what thread we'll be called from
        Realm realm = Realm.getDefaultInstance();
    }
}
