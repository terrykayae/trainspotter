package uk.co.tezk.trainspotter;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Utility helper classes
 */

public class Utilitity {
    public static boolean isLandscape(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        //int densityDpi = metrics.densityDpi;

        return (widthPixels > heightPixels);
    }
}
