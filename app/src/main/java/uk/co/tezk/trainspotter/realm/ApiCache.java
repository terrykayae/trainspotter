package uk.co.tezk.trainspotter.realm;


import android.util.Log;

import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.Observer;
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

    public void cacheClassList(Observable<ClassNumbers> classNumbersObservable) {

        if (realm != null) {
            // Data is currently being persisted, wait. Very messy. TODO: implement better soution
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (realm != null);
        }

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
                    public void onNext(String classNumber) {
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

    public void cacheTrainList(Observable<List<TrainListItem>> trainsObservable) {
        if (realm != null) {
            // Data is currently being persisted, wait. Very messy. TODO: implement better soution
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (realm != null);
        }
        realm = null;
        compositeSubscription.add(trainsObservable
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<List<TrainListItem>, Observable<TrainListItem>>() {
                    @Override
                    public Observable<TrainListItem> call(List<TrainListItem> trainListItems) {
                        return Observable.from(trainListItems);
                    }
                })
                .subscribe(new Observer<TrainListItem>() {
                    @Override
                    public void onCompleted() {
                        Log.i("API","save trains complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("API","error "+e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(final TrainListItem trainListItem) {
                        if (realm == null)
                            realm = Realm.getDefaultInstance();
                        Log.i("API","Saving "+trainListItem);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealm(trainListItem);
                            }
                        });
                    }
                }));
    }

    public void unsubscribe() {
        if (compositeSubscription != null && compositeSubscription.hasSubscriptions())
            compositeSubscription.unsubscribe();
    }
}
