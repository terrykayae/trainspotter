package uk.co.tezk.trainspotter.realm;


import android.util.Log;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.tezk.trainspotter.model.ClassDetails;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.TrainListItem;

/**
 * Used by the API to persist Observables to the Realm database
 */

public class ApiCache {
    private static ApiCache apiCache;
    private Realm realm;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static synchronized ApiCache getInstance() {
        if (apiCache == null) {
            apiCache = new ApiCache();
        }
        return apiCache;
    }

    public void  cacheClassList(Observable<ClassNumbers> classNumbersObservable) {

        synchronized (this) {
            if (realm != null) {
                // Data is currently being persisted, wait. Very messy. TODO: implement better soution
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("API", "wait loop ccl");
                } while (realm != null);
            }

            // Clear the DB
            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(ClassDetails.class).findAll().deleteAllFromRealm();
                }
            });

            compositeSubscription.add(classNumbersObservable
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
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i("ApiCache", "error " + e.getMessage());
                            realm = null;
                        }

                        @Override
                        public void onNext(final String classNumber) {
                            if (realm == null)
                                realm = Realm.getDefaultInstance();
                            final ClassDetails classDetails = new ClassDetails();
                            classDetails.setClassId(classNumber);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealmOrUpdate(classDetails);
                                }
                            });
                        }
                    }));
        }
    }

    public void cacheTrainList(Observable<List<TrainListItem>> trainsObservable) {
        synchronized (this) {
            Log.i("API", "CacheTrainList");
            if (realm != null) {
                Log.i("API", "Realm = " + realm);
                // Data is currently being persisted, wait. Very messy. TODO: implement better soution
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("API", "wait loop ctl");
                } while (realm != null);
            }
            realm = null;
            Observable<Integer> count = trainsObservable.count();

            Log.i("API", "Carrying out save");
            compositeSubscription.add(trainsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<List<TrainListItem>, Observable<TrainListItem>>() {
                        @Override
                        public Observable<TrainListItem> call(List<TrainListItem> trainListItems) {
                            return Observable.from(trainListItems);
                        }
                    })
                    .subscribe(new Observer<TrainListItem>() {
                        @Override
                        public void onCompleted() {
                            Log.i("API", "save trains complete");
                            realm = null;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i("API", "error " + e.getMessage());
                            e.printStackTrace();
                            realm = null;
                        }

                        @Override
                        public void onNext(final TrainListItem trainListItem) {
                            if (realm == null)
                                realm = Realm.getDefaultInstance();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    if (realm.where(TrainListItem.class).equalTo("_class", trainListItem.getClass_()).equalTo("number", trainListItem.getNumber())
                                            .count() <= 0)
                                        realm.copyToRealm(trainListItem);

                                }
                            });
                        }
                    }));
        }
    }

    public void unsubscribe() {
        if (compositeSubscription != null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
        compositeSubscription = null;
        apiCache = null;
    }
}
