package uk.co.tezk.trainspotter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.co.tezk.trainspotter.model.Camera;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.view.LogSpotFragment;

import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.TAKE_PHOTO;

public class LogSpotActivity extends AppCompatActivity {
    FloatingActionButton fab;
    private LogSpotFragment fragment;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_spot);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragment.handleSave())
                    finish();
            }
        });

        fragment = new LogSpotFragment();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String trainClass = bundle.getString(Constant.CLASS_NUM_KEY);
            String trainNumber = bundle.getString(Constant.TRAIN_NUM_KEY);
            fragment.setTrainClass(trainClass);
            fragment.setTrainNum(trainNumber);
            String imagePath = bundle.getString(Constant.IMAGES_KEY);
            if (imagePath != null) {
                fragment.setImage(imagePath);
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.logSpotFragmentHolder, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_camera) {
            mCamera = new Camera(this,
                    Constant.REQUEST_IMAGE_CAPTURE_FROM_SPOT,
                    Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT);
            mCamera.takePicture();

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Show about dialog
        if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle("TrainSpotter " + BuildConfig.VERSION_NAME)
                    .setMessage("Images courtesy of www.freepik.com\nused under their \"free licence\"\nTo report issues or\ninacuracies please email\ntrainspotter@tezk.co.uk")
                    .setNegativeButton("More details", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("http:\\www.freepik.com"));
                            startActivity(intent);
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the permission dialog is closed
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // location-related task you need to do.
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    case MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT: {
                        fragment.getmGoogleMap().setMyLocationEnabled(true);
                    }
                    break;
                    case MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT: {
                        fragment.onClick(TAKE_PHOTO);
                    }
                    break;
                }
            }
        }
    }

    // Camera callback
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_SPOT && resultCode == RESULT_OK) {
            Log.i("MA", "passing image to Log fragment : " + fragment);
            // Was image request sent from within LogSpot fragment? If so, pass on the message
            fragment.setCamera(mCamera);
            fragment.onImageReady();
        }
    }
}
