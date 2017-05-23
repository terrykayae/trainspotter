package uk.co.tezk.trainspotter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.realm.Realm;
import uk.co.tezk.trainspotter.interfaces.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.Camera;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.parcel.TrainParcel;
import uk.co.tezk.trainspotter.realm.ApiCache;
import uk.co.tezk.trainspotter.view.ClassListFragment;
import uk.co.tezk.trainspotter.view.LogSpotFragment;
import uk.co.tezk.trainspotter.view.TrainDetailFragment;
import uk.co.tezk.trainspotter.view.TrainListFragment;

import static android.R.drawable;
import static uk.co.tezk.trainspotter.Utilitity.isLandscape;
import static uk.co.tezk.trainspotter.model.Constant.CLASS_NUM_KEY;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.CLASS_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INITIALISING;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INVALID;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.LOG_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_DETAIL;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION_KEY;
import static uk.co.tezk.trainspotter.model.Constant.IMAGES_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.PICK_IMAGE_FROM_GALLERY;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE_FROM_MAIN;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.TAKE_PHOTO;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_NUM_KEY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ClassListFragment.OnClassListFragmentInteractionListener,
        TrainListFragment.OnTrainListFragmentInteractionListener,
        TrainDetailFragment.OnTrainDetailFragmentInteractionListener,
        TrainspotterDialogSupport

{

    FloatingActionButton fab;
    CURRENT_ACTION currentAction;

    private Context context;
    // Store actions! If user pressed back, calling popBackStack() returns to previous fragment, but we don't know what activity that is
    // Store last activity here before updating
    private Stack<CURRENT_ACTION> actionStack = new Stack();
    // Save a reference to the last parameters
    private Map<String, String> lastParams;
    // Set when views are loaded to determine layout
    private boolean landscape;
    private boolean tablet;

    private boolean logFromImage = false;

    private Fragment fragment;
    // If we're loading two fragments, this is what and where for the second
    Fragment secondFragment;

    // Holder for the camera which will hold the filename if we start Camera from the NavigationDrware
    private Camera mCamera;

    // If we're started from a notification, we received a Train parcel. Store that here to display at the right time
    TrainParcel trainParcel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentAction = INITIALISING;
        context = this;
        // Initalise the action stack - we don't push the current state from onResume, so need first element to be CLASS_LIST
        if (actionStack.size() == 0)
            actionStack.push(CLASS_LIST);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setFabSpot();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.getInt(CURRENT_ACTION_KEY) > 0) {
                // We were doing something and got recreated - set currentAction so onResume() knows what to do
                currentAction = CURRENT_ACTION.fromInteger(savedInstanceState.getInt(CURRENT_ACTION_KEY));
                if (currentAction == INVALID) {
                    currentAction = INITIALISING;
                }
            }
        }
        Bundle bundle = getIntent().getExtras();
        if (getIntent().getParcelableExtra(Constant.TRAIN_PARCEL_KEY) != null) {
            Log.i("MA", "Show train details from notification...");
            // We're to show a train!
            currentAction = TRAIN_DETAIL;
            trainParcel = getIntent().getParcelableExtra(Constant.TRAIN_PARCEL_KEY);
        } else {
            if (bundle != null && bundle.get("class") != null) {
                Log.i("MA", "rebuilding parcelable");
                float lat = Float.parseFloat((String) bundle.get("lat"));
                float lon = Float.parseFloat((String) bundle.get("lon"));
                String classNum = (String) bundle.get("class");
                String trainNum = (String) bundle.get("num");

                trainParcel = new TrainParcel(lat, lon, classNum, trainNum);
                currentAction = TRAIN_DETAIL;
            }
        }

        if (bundle != null) {
            for (String each : bundle.keySet()) {
                Log.i("MA", each + " = " + bundle.get(each));
            }
        } else Log.i("MA", "No intent!");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MA", "onStart");
        // Close any progress dialogues and fade the background
        doneInitialising();
        if (currentAction == INITIALISING)
            currentAction = CLASS_LIST;

        // Load fragment, but don't add to the backstack - if we did on every onResume, we get a stack full!
        loadFragment(currentAction, false, null);

        // if we started camera from navigation drawer, we can't load in the new fragment until we get here to
        // Log the image!
        if (logFromImage) {
            Log.i("MA", "onCreate, logFromImage, camera = " + mCamera);
            Map params = new HashMap();
            params.put(IMAGES_KEY, mCamera.getFilename());
            logFromImage = false;
            loadFragment(LOG_SPOT, true, params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MA", "onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();
        ApiCache.getInstance().unsubscribe();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Is the navigation drawer open? Close if it is!
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Navigation drawer is closed, move back one fragment, quit if the stack is empty
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

                getSupportFragmentManager().popBackStack();

                if (actionStack.size() > 0) {
                    // Should never be 0 if we're popping - if last action was Spot, change the FAB button back
                    Log.i("MA", "pressed back = " + actionStack.peek());
                    if (actionStack.pop() == LOG_SPOT) {
                        setFabSpot();
                    }
                    if (actionStack.size() == 0) {
                        Log.e("MA", "actionStack was empty");
                        actionStack.push(CLASS_LIST);
                    }
                    currentAction = actionStack.peek();
                    fragment = getSupportFragmentManager().findFragmentByTag("MAIN_FRAGMENT");
                    Log.i("MA", "Popped backstack = " + fragment);
                }
            } else
                finish();
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Show about dialog
        if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle("TrainSpotter " + BuildConfig.VERSION_NAME)
                    .setMessage("Images courtesy of www.freepik.com\nused under their \"free licence\"\n")
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            mCamera = new Camera(this,
                    Constant.REQUEST_IMAGE_CAPTURE_FROM_MAIN,
                    Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN);
            mCamera.takePicture();
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
        } else if (id == R.id.nav_favourites) {
            // Todo : Show list of favourites


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void doneInitialising() {
        //Called when we've completed the initialisation process - hide any progress dialogs, change the back
        //image to opaque
       // findViewById(R.id.ivMallard).setAlpha(0.3f);
        ((RelativeLayout)findViewById(R.id.mainContainer)).setBackgroundColor(getResources().getColor(R.color.main_bg));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save currentAction
        outState.putInt(CURRENT_ACTION_KEY, CURRENT_ACTION.toInteger(currentAction));
        super.onSaveInstanceState(outState);
    }


    private void loadFragment(CURRENT_ACTION action, boolean addToBackStack, Map<String, String> params) {
        //Reset the fragment holder
        //fragment = null;
        Log.i("MA", "Loading fragment " + action + " with addToBackStack " + addToBackStack);
        Log.i("MA", "fragment = " + fragment + ", currentAction = " + currentAction);
        secondFragment = null;
        int secondViewId = 0;
        if (params != null)
            lastParams = params;
        // Check for multi fragment layout
        landscape = isLandscape(this);
        if (findViewById(R.id.landscapeLayout)!=null) {
            // Landscape view found, load fragment
            tablet = true;
        }

        // TODO : Find more elegant way of finding whether we're tablet


        //setFabSpot();

        switch (action) {
            case CLASS_LIST:
                fragment = new ClassListFragment();
                if (landscape) {
                    // We're landscape so need to load train list as well
                    secondViewId = R.id.trainListFragmentHolder;
                    TrainListFragment mTrainlistFragment = new TrainListFragment();
                    mTrainlistFragment.forcePortrait();
                    String classToShow = (params == null ? "1" : params.get(CLASS_NUM_KEY));
                    Log.i("MA", "loadFragment, showing class in secondFragment " + classToShow);
                    mTrainlistFragment.setShowTrainsForClass(classToShow);

                    secondFragment = mTrainlistFragment;
                }

                break;
            case LOG_SPOT:
                setFabSave();
                // Only load a new fragment if we're not restoring a LogSpotFragment - only layout changes, not configuration of panels
                if (fragment == null || !(fragment instanceof LogSpotFragment)) {
                    fragment = new LogSpotFragment();
                    if (params != null) {
                        if (params.get(IMAGES_KEY) != null) {
                            Log.i("MA", "Setting logspot to show image : " + params.get(IMAGES_KEY));
                            ((LogSpotFragment) fragment).setImage(params.get(IMAGES_KEY));
                        } else {
                            ((LogSpotFragment) fragment).setTrainClass(params.get(CLASS_NUM_KEY));
                            ((LogSpotFragment) fragment).setTrainNum(params.get(TRAIN_NUM_KEY));
                        }
                    }
                } else return;
                break;
            case TRAIN_LIST:
                if (!(fragment instanceof TrainListFragment)) {
                    TrainListFragment mTrainListFragment = new TrainListFragment();
                    fragment = mTrainListFragment;
                }
                if (params != null) {
                    String classToShow = params.get(CLASS_NUM_KEY);
                    Log.i("MA", "loadFragment, (port) showing class " + classToShow);
                    if (classToShow != null) {
                        ((TrainListFragment) fragment).setShowTrainsForClass(classToShow);
                    }
                } else {
                    Log.i("MA", "call to load trainList with null arguments, using " + lastParams);
                    if (lastParams == null) {
                        // Last resort = if we can't get details of class we were showing, show class list
                        loadFragment(CLASS_LIST, true, null);
                        return;
                    }
                    String classToShow = lastParams.get(CLASS_NUM_KEY);

                    Log.i("MA", "loadFragment, (port) showing class " + classToShow);
                    if (classToShow != null) {
                        ((TrainListFragment) fragment).setShowTrainsForClass(classToShow);
                    }
                }

                if (landscape) {
                    // If we're landscape, the train list goes on the right, class list on the left
                    TrainDetailFragment mTrainDetailFragment = new TrainDetailFragment();
                    mTrainDetailFragment.forcePortrait(true);
                    if (params != null) {
                        String classToShow = params.get(CLASS_NUM_KEY);
                        String trainToShow = params.get(TRAIN_NUM_KEY);
                        mTrainDetailFragment.setShowDetailsForTrain(classToShow, trainToShow);
                    }
                    secondFragment = mTrainDetailFragment;
                    secondViewId = R.id.trainDetailFragmentHolder;
                }
                break;
            case TRAIN_DETAIL:
                // if landscape, train details on the right, train list on left, unless showing notification
                // Then fill the screen
                if (landscape && trainParcel == null) {
                    if (!(fragment instanceof TrainListFragment)) {
                        TrainListFragment mTrainListFragmentForDetail = new TrainListFragment();
                        fragment = mTrainListFragmentForDetail;
                        String classToShowForDetail = (params == null ? "1" : params.get(CLASS_NUM_KEY));
                        mTrainListFragmentForDetail.setShowTrainsForClass(classToShowForDetail);
                        secondFragment = new TrainDetailFragment();
                        ((TrainDetailFragment) secondFragment).forcePortrait(true);
                        secondViewId = R.id.trainDetailFragmentHolder;
                        Log.i("MA", "loadFragment, (land, traininfo) showing class " + classToShowForDetail);
                    }
                } else {
                    // is fragment current TrainDetailFragment? Might be if we've been paused. Only load new if not
                    if (!(fragment instanceof TrainDetailFragment)) {
                        fragment = new TrainDetailFragment();
                    }
                }
                if (params != null) {
                    String classNum = params.get(CLASS_NUM_KEY);
                    String trainNum = params.get(TRAIN_NUM_KEY);
                    TrainDetailFragment trainDetailFragment;
                    if (fragment instanceof TrainDetailFragment)
                        trainDetailFragment = (TrainDetailFragment) fragment;
                    else
                        trainDetailFragment = (TrainDetailFragment) secondFragment;
                    trainDetailFragment.setShowDetailsForTrain(classNum, trainNum);
                }
                // Are we showinf a notification?
                if (trainParcel != null) {
                    ((TrainDetailFragment) fragment).setNotifyFor(trainParcel);
                    trainParcel = null;
                }
                break;
            default:
                throw new RuntimeException("Operation not supported");
        }


        Log.i("MA", "currentActivity = " + (currentAction == null ? "null" : currentAction));
        Log.i("MA", "fragment = " + (fragment == null ? "null" : fragment));
        Log.i("MA", "secondFragemt = " + (secondFragment == null ? "null" : secondFragment));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setEnterTransition(new Explode());
            fragment.setReturnTransition(new Explode());
        }

        // Swap in the fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.mainContainer, fragment, "MAIN_FRAGMENT");
        if (secondFragment != null) {
            transaction.replace(secondViewId, secondFragment, "SECOND_FRAGMENT");
        }


        if (addToBackStack) {
            transaction.addToBackStack(null);
            actionStack.push(action);
        }

        transaction.commit();

        tablet = false;
        landscape = false;
        // Now that the fragment has loaded, check for the presence of Landscape view holders which indicate we're
        // on a tablet

        if (findViewById(R.id.landscapeLayout) != null) {
            // Landscape train list! Load train details fragment into holder
            tablet = true;
            landscape = true;
        }

        Log.i("MA", "fragment loaded, tablet = " + tablet + ", landscape = " + landscape);
        currentAction = action;
    }

    @Override
    public void onDisplayTrainsInClass(String classNum) {
        // Handler for click events from the class list
        Map args = new HashMap();
        args.put(CLASS_NUM_KEY, classNum);
        if (landscape) {
            // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
            ((TrainListFragment) secondFragment).setShowTrainsForClass(classNum);
            ((TrainListFragment) secondFragment).reloadTrainList();
        } else {
            currentAction = TRAIN_LIST;
            loadFragment(currentAction, true, args);
        }
        Log.i("MA", "display trains in class " + classNum);
        //View view = findViewById(R.id.trainListFragmentHolder);
    }


    // Handler for interaction with TrainListFragment
    @Override
    public void onShowTrainDetails(String classNum, String trainNum) {
        // TODO : depending on layout, if Tablet, left pane = train list, right = train details
        // TODO : if phone, load in train details fragment
        Log.i("MA", "onShowTrainDetails " + classNum + ", " + trainNum);
        Map args = new HashMap();
        args.put(CLASS_NUM_KEY, classNum);
        args.put(TRAIN_NUM_KEY, trainNum);
        if (landscape) {
            if (secondFragment instanceof TrainDetailFragment) {
                // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
                ((TrainDetailFragment) secondFragment).setShowDetailsForTrain(classNum, trainNum);
                //((TrainDetailFragment)secondFragment).reloadTrainList();
            } else {
                // need to load train list, add us to the right
                loadFragment(TRAIN_LIST, true, args);
                // Now display
                if (secondFragment instanceof TrainDetailFragment) {
                    ((TrainDetailFragment) secondFragment).setShowDetailsForTrain(classNum, trainNum);
                } else {
                    Log.i("MA", "onShowTrainDetails = Second fragment still not details?");
                }
            }
        } else {
            loadFragment(TRAIN_DETAIL, true, args);
        }
        Log.i("MA", "onShowTrainDetails display train " + classNum + ", " + trainNum


        );
    }

    @Override
    public void onAddSightingForTrain(String classNum, String trainNum) {
        // Called if we click to add a sighting when we're displaying a trains details
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown realm
        Realm.getDefaultInstance().close();
        ApiCache.getInstance().unsubscribe();
    }
    // Helper methods to alter the behaviour of the FAB

    private void setFabSpot() {
        // The usual FAB action is to add a sighting
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> params = null;
                // Load the "Spotting" fragment, save on the backstack
                if (currentAction == TRAIN_DETAIL) {
                    // we're showing a train? If so, add log for that one
                    TrainDetail currentTrain = ((TrainDetailFragment) fragment).getCurrentTrain();
                    if (currentTrain != null && currentTrain.getTrain() != null) {
                        params = new HashMap();
                        params.put(CLASS_NUM_KEY, currentTrain.getTrain().getClass_());
                        params.put(TRAIN_NUM_KEY, currentTrain.getTrain().getNumber());
                    }
                }
                loadFragment(CURRENT_ACTION.LOG_SPOT, true, params);
            }
        });
    }

    private void setFabSave() {
        // When the Add Sighting fragment is shown, FAB should save the sighting
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_save));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragment instanceof LogSpotFragment) {
                    //Get the fragment
                    LogSpotFragment logSpotFragment = (LogSpotFragment) fragment;
                    //And save - if returns true it has saved, so we can close fragment by simulating Back Pressed
                    if (logSpotFragment.handleSave())
                        onBackPressed();
                } else {
                    Log.i("MA", "Save pressed on none savable screen?");
                }
            }
        });
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
                        Log.i("MA", "permission call back from logspot, fragment = " + fragment);
                        if (fragment instanceof LogSpotFragment) {
                            Log.i("MA", "setting location enabled on log spot to true");
                            ((LogSpotFragment) fragment).getmGoogleMap().setMyLocationEnabled(true);
                        }
                    }
                    break;
                    case MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS: {
                        if (fragment instanceof TrainDetailFragment) {
                            ((TrainDetailFragment) fragment).getGoogleMap().setMyLocationEnabled(true);
                        }
                    }
                    break;
                    case MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN: {
                        mCamera = new Camera(this,
                                Constant.REQUEST_IMAGE_CAPTURE_FROM_MAIN,
                                Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN);
                        mCamera.takePicture();
                    }
                    break;
                    case MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT: {
                        if (fragment instanceof LogSpotFragment) {
                            // Tell LogSpot fragment to try taking another photo
                            ((LogSpotFragment) fragment).onClick(TAKE_PHOTO);
                        }
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
            if (fragment instanceof LogSpotFragment) {
                ((LogSpotFragment) fragment).onImageReady();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_MAIN && resultCode == RESULT_OK) {
            // Was image request sent from MainActivity? Start LogSpot to save it!
            Log.i("MA", "image " + mCamera.getFilename() + " is ready for access! Starting LogSpot");
            // Set a flag so when we get back to onResume, we can load in the new Fragment
            logFromImage = true;
            //  Map params = new HashMap();
            //  params.put(IMAGES_KEY, mCamera.getFilename());
            //    loadFragment(LOG_SPOT, true, params);
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            // Image picked!
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
        }
    }

    // TrainspotterDialog implementation, allows fragments to show progress and errors without worrying about
    // implementation details and so be consistent

    private ProgressDialog progressDialog;

    public void startProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.loading_message));
        }
        progressDialog.show();
    }

    public void stopProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, "Encountered an error:\n" + message, Toast.LENGTH_LONG).show();
    }
}
