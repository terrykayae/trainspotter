package uk.co.tezk.trainspotter.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import uk.co.tezk.trainspotter.Utilitity;

/**
 * Class to deal with camera and associated permissions needed to access external storage
 * Params : Context = activity that will receive call backs for permission requests and camera results
 * requestCode = requestCode that will be sent to onActivityResult
 * permissionCode = permissionCode that will be sent to onRequestPermissionResult
 */

public class Camera {
    private Context context;
    private int requestCode;
    private int permissionCallbackCode;

    String imageFilename;

    public Camera(Context context, int requestCode, int permissionCode) {
        this.context = context;
        this.requestCode = requestCode;
        this.permissionCallbackCode = permissionCode;
    }

    public String getFilename() { return imageFilename; }

    public void setFilename(String imageFilename)
    { this.imageFilename = imageFilename; }

    public String takePicture() {
        imageFilename = null;
        // Check we can write externally, if so send request to take a picture
        // requestCode will be sent to onActivityResult() of Context when complete
        // if we don't yet have permisson, permissionCallBack code will be sent to
        // onRequestPermissionResult() of context will be called
        // Returns Filename for image, or null on error

        if (Utilitity.checkStoragePermissions(context, permissionCallbackCode)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File imageFile = null;
                try {
                    imageFile = Utilitity.createImageFile(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (imageFile != null) {
                    Log.i("LSF", "Image will save at : " + imageFile.getAbsolutePath());

                    Uri photoURI = FileProvider.getUriForFile(
                            context,
                            "uk.co.tezk.trainspotter",
                            imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    imageFilename = imageFile.getAbsolutePath();
                }
                ((Activity) context).startActivityForResult(takePictureIntent, requestCode);
            }
        }
        return imageFilename;
    }

    public void addToGallery() {
        addToGallery(imageFilename);
    }

    public void addToGallery(String imageFilename) {
        if (imageFilename == null)
            return;
        MediaScannerConnection.scanFile(context,
                new String[]{imageFilename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                              Log.i("ExternalStorage", "Scanned " + path + ":");
                              Log.i("ExternalStorage", "-> uri=" + uri);
                    }

                });
        }
    }
