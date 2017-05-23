package uk.co.tezk.trainspotter.model;

import android.content.Context;
import android.content.SharedPreferences;

import uk.co.tezk.trainspotter.TrainSpotterApplication;

/**
 * Created by tezk on 23/05/17.
 */

public class TrainspotterSharedPreferences {
    private static final String sharedPrefsName = "uk.co.tezk.trainspotter.SHAREDPREFS";
    private static final String CLASS_KEY = "class";
    private static final String TRAIN_KEY = "train";

    public static void setClass(String classNumber) {
        TrainSpotterApplication application = TrainSpotterApplication.getApplication();
        SharedPreferences sharedPreferences = application.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CLASS_KEY, classNumber);
        editor.commit();
    }

    public static void setTrain(String classNumber, String trainNumber) {
        SharedPreferences sharedPreferences = TrainSpotterApplication.getApplication().getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CLASS_KEY, classNumber);
        editor.putString(TRAIN_KEY, trainNumber);
        editor.commit();
    }

    public static String getClassNumber() {
        SharedPreferences sharedPreferences = TrainSpotterApplication.getApplication().getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(CLASS_KEY, null);
    }

    public static String getTrainNumber() {
        SharedPreferences sharedPreferences = TrainSpotterApplication.getApplication().getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TRAIN_KEY, null);
    }
}
