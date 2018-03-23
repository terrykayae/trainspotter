package uk.co.tezk.trainspotter.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.model.Constant;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, ACCESS_FINE_LOCATION)) {
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
                        permissionCallbackCode);
            }
        } else {
            return true;
        }
        return false;
    }

    public static boolean checkStoragePermissions(final Context context, final int permissionCallbackCode) {
        if (ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // We haven't got permission to access FINE_LOCATION, do we need to provide an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, WRITE_EXTERNAL_STORAGE)) {
                // Show a dialog to explain why we need permission
                new AlertDialog.Builder(context)
                        .setTitle("External write permission")
                        .setMessage("This app needs the External Storage permission to save photos")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // When user clicks OK, ask for permission
                                ActivityCompat.requestPermissions((MainActivity) context,
                                        new String[]{WRITE_EXTERNAL_STORAGE},
                                        permissionCallbackCode);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No need to prompt the user, just ask for permission
                ActivityCompat.requestPermissions((MainActivity) context,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        permissionCallbackCode);
            }
        } else {
            return true;
        }
        return false;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TrainSpot_" + timeStamp;
        //File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir = new File(storageDir.getAbsoluteFile().toString()+"/TrainSpotter");
        Log.i("Utility","mkdir = "+storageDir.mkdir());
        Log.i("Utility", "paht is "+storageDir.getAbsolutePath());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.i("Utility", "filename is "+image.getAbsolutePath());

        // Save a file: path for use with ACTION_VIEW intents
        // 0mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    public static boolean isNetworkAvailable() {
        Context context = TrainSpotterApplication.getApplication();
                // return true if network is connecte
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED);
    }

    public static String getClassNameFromNumber(int number) {
        int classes [][][]={Constant.DIESEL,Constant.ELECTRIC, Constant.DMU, Constant.EMU, Constant.DEMU};
        String classNames [] = {Constant.DIESEL_TITLE, Constant.ELECTRIC_TITLE, Constant.DMU_TITLE, Constant.EMU_TITLE, Constant.DEMU_TITLE};

        String name = "Steam";
        for (int c = 0; c< classes.length; c++) {
            for (int l = 0; l<classes[c].length; l++) {
                if (number >= classes[c][l][0] && number <= classes[c][l][1]) {
                    name = classNames[c];
                    break;
                }
            }
        }
        return name;
    }
}
