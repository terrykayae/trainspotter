package uk.co.tezk.trainspotter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;

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

    public static boolean checkLocationPermissions(final Context context, final int permissionCallbackCode) {
        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // We haven't got permission to access FINE_LOCATION, do we need to provide an explanation?
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && ((MainActivity) context).shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                // Show a dialog to explain why we need permission
                new AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // When user clicks OK, ask for permission
                                ActivityCompat.requestPermissions((MainActivity) context,
                                        new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                                        permissionCallbackCode);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No need to prompt the user, just ask for permission
                ActivityCompat.requestPermissions((MainActivity) context,
                        new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT);
            }
        } else {
            return true;
        }
        return false;
    }


    private File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // 0mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
