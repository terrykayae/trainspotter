package uk.co.tezk.trainspotter;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import uk.co.tezk.trainspotter.interfaces.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.Camera;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainspotterSharedPreferences;
import uk.co.tezk.trainspotter.model.parcel.TrainParcel;
import uk.co.tezk.trainspotter.realm.ApiCache;
import uk.co.tezk.trainspotter.view.ClassListFragment;
import uk.co.tezk.trainspotter.view.TrainDetailFragment;
import uk.co.tezk.trainspotter.view.TrainListFragment;

import static android.R.drawable;
import static uk.co.tezk.trainspotter.model.Constant.CLASS_NUM_KEY;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.CLASS_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INITIALISING;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INVALID;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_DETAIL;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS;
import static uk.co.tezk.trainspotter.model.Constant.PICK_IMAGE_FROM_GALLERY;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE_FROM_MAIN;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_NUM_KEY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ClassListFragment.OnClassListFragmentInteractionListener,
        TrainListFragment.OnTrainListFragmentInteractionListener,
        TrainDetailFragment.OnTrainDetailFragmentInteractionListener,
        TrainspotterDialogSupport

{
    CURRENT_ACTION currentAction;

    private Context context;
    // Set when views are loaded to determine layout
    private boolean landscape;
    private boolean tablet;

    private Fragment fragment;
    // If we're loading two fragments, this is what and where for the second
    private Fragment secondFragment;

    // Holder for the camera which will hold the filename if we start Camera from the NavigationDrware
    private Camera mCamera;

    // If we're started from a notification, we received a Train parcel. Store that here to display at the right time
    TrainParcel trainParcel;

    // Holders for current train and class
    private String currentTrain;
    private String currentClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentAction = INITIALISING;
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
            Map params = new HashMap();
            if (currentTrain != null) {
                params.put(Constant.CLASS_NUM_KEY, currentClass);
                params.put(Constant.TRAIN_NUM_KEY, currentTrain);
            } else {
                if (savedInstanceState.getString(CLASS_NUM_KEY) != null) {
                    currentClass = savedInstanceState.getString(CLASS_NUM_KEY);
                    params.put(Constant.CLASS_NUM_KEY, currentClass);
                }
                if (savedInstanceState.getString(TRAIN_NUM_KEY) != null) {
                    currentTrain = savedInstanceState.getString(TRAIN_NUM_KEY);
                    params.put(Constant.TRAIN_NUM_KEY, currentTrain);
                }


            }
            loadFragment(currentAction, false, params);
        } else {
            Log.i("MA", "savedInstanceState was null");
            Log.i("MA", "onCreate, resetting class list");
            if (fragment == null)
                loadFragment(CLASS_LIST, false, null);

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
                String lat = (String) bundle.get("lat");
                String lon = (String) bundle.get("lon");
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

        Log.i("MA", "onCreate, fragmentBackstack = " + getSupportFragmentManager().getBackStackEntryCount());
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
        Map params = new HashMap();
        if (trainParcel != null) {
            params.put(Constant.CLASS_NUM_KEY, trainParcel.getTrainClass());
            params.put(Constant.TRAIN_NUM_KEY, trainParcel.getTrainNum());
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
            } else
                finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.app_bar_search));
        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            searchView.setSearchableInfo(searchManager.
                    getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);

            final PublishSubject<String> subject = PublishSubject.create();


            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("MA", "onQueryTextChanged = " + newText);
                    subject.onNext(newText.toString());

                    return true;
                }
            });
            subject.debounce(600, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            Log.i("MA", "SearchHandler = onComplete?");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i("MA", "SearchHandler = error? " + e.getMessage());
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(String s) {
                            if (s == null || s.length() == 0) {
                                if (currentAction == TRAIN_LIST) {
                                    TrainListFragment trainListFragment = (TrainListFragment) fragment;
                                    trainListFragment.searchFor("");
                                }
                                return;
                            }
                            if (currentAction != TRAIN_LIST)
                                loadFragment(TRAIN_LIST, true, null);
                            Log.i("MA", "Search for " + s);
                            TrainListFragment trainListFragment = (TrainListFragment) fragment;
                            trainListFragment.searchFor(s);
                        }
                    });
        } else {
            Log.i("MA", "SearchView was null");
        }
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
                    Constant.REQUEST_IMAGE_CAPTURE_FROM_MAIN,
                    Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_MAIN);
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
        ((LinearLayout) findViewById(R.id.parentContainer)).setBackgroundColor(getResources().getColor(R.color.main_bg));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save currentAction
        outState.putInt(CURRENT_ACTION_KEY, CURRENT_ACTION.toInteger(currentAction));
        // Save details of most recent train / class
        outState.putString(CLASS_NUM_KEY, currentClass);
        outState.putString(TRAIN_NUM_KEY, currentTrain);

        Log.i("MA", "onSaveInstanceState, fragmentBackstack = " + getSupportFragmentManager().getBackStackEntryCount());

    }

    private void loadFragment(CURRENT_ACTION action, boolean addToBackStack, Map<String, String> params) {
        Log.i("MA", "Loading fragment " + action + " with addToBackStack " + addToBackStack);
        Log.i("MA", "fragment = " + fragment + ", currentAction = " + currentAction);

        //secondFragment = null;

        if (params == null) {
            params = new HashMap<>();
            if (TrainspotterSharedPreferences.getClassNumber() != null)
                params.put(CLASS_NUM_KEY, TrainspotterSharedPreferences.getClassNumber());
            if (TrainspotterSharedPreferences.getTrainNumber() != null)
                params.put(TRAIN_NUM_KEY, TrainspotterSharedPreferences.getTrainNumber());
        }

        currentClass = params.get(Constant.CLASS_NUM_KEY);
        currentTrain = params.get(Constant.TRAIN_NUM_KEY);

        if (findViewById(R.id.landscapeLayout) != null) {
            // Landscape view found, load fragment
            tablet = true;
            landscape = true;
        }

        // TODO : Find more elegant way of finding whether we're tablet

        String fragmentOneTag = null;
        String fragmentTwoTag = null;

        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (action) {
            case LOG_SPOT:
                doLogSpot();
                break;
            case CLASS_LIST:
                fragment = fragmentManager.findFragmentByTag("CLASSLIST");
                if (fragment == null)
                    fragment = new ClassListFragment();
                fragmentOneTag = "CLASSLIST";
                Log.i("MA", "Set to load class list");
                if (landscape) {
                    // We're landscape so need to load train list as well
                    TrainListFragment mTrainlistFragment = (TrainListFragment) fragmentManager.findFragmentByTag("TRAINLIST");
                    if (mTrainlistFragment == null)
                        mTrainlistFragment = new TrainListFragment();
                    String classToShow = (params == null ? "1" : params.get(CLASS_NUM_KEY));
                    mTrainlistFragment.setShowTrainsForClass(classToShow);

                    secondFragment = mTrainlistFragment;
                    fragmentTwoTag = "TRAINLIST_LAND";
                    Log.i("MA", "Set to load train list " + landscape);
                }
                break;

            case TRAIN_LIST:
                fragment = fragmentManager.findFragmentByTag("TRAINLIST");
                if (fragment == null) {
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
                    // Last resort = if we can't get details of class we were showing, show class list
                    loadFragment(CLASS_LIST, true, null);
                    return;
                }

                fragmentOneTag = "TRAINLIST";
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
                    fragmentTwoTag = "DETAIL";
                }
                break;
            case TRAIN_DETAIL:
                // if landscape, train details on the right, train list on left, unless showing notification
                // Then fill the screen
                if (landscape && trainParcel == null) {
                    fragment = fragmentManager.findFragmentByTag("TRAINLIST");
                    if (fragment == null) {
                        TrainListFragment mTrainListFragmentForDetail = new TrainListFragment();
                        fragment = mTrainListFragmentForDetail;
                        String classToShowForDetail = (params == null ? "1" : params.get(CLASS_NUM_KEY));
                        mTrainListFragmentForDetail.setShowTrainsForClass(classToShowForDetail);
                        secondFragment = new TrainDetailFragment();
                        ((TrainDetailFragment) secondFragment).forcePortrait(true);
                        Log.i("MA", "loadFragment, (land, traininfo) showing class " + classToShowForDetail);
                        fragmentOneTag = "TRAINLIST";
                        fragmentTwoTag = "DETAIL";
                    }
                } else {
                    // is fragment current TrainDetailFragment? Might be if we've been paused. Only load new if not
                    fragment = fragmentManager.findFragmentByTag("DETAIL");
                    if (fragment == null) {
                        fragment = new TrainDetailFragment();
                        fragmentOneTag = "DETAIL";
                    }
                }
                if (params != null) {
                    String classNum = params.get(CLASS_NUM_KEY);
                    String trainNum = params.get(TRAIN_NUM_KEY);
                    TrainDetailFragment trainDetailFragment;
                    if (fragment instanceof TrainDetailFragment)
                        trainDetailFragment = (TrainDetailFragment) fragment;
                    else {
                        if (secondFragment instanceof TrainDetailFragment)
                            trainDetailFragment = (TrainDetailFragment) secondFragment;
                        else
                            trainDetailFragment = new TrainDetailFragment();
                    }
                    trainDetailFragment.setShowDetailsForTrain(classNum, trainNum);
                }
                // Are we showing a notification?
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

        // Swap in the fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainContainer, fragment, fragmentOneTag);

        if (secondFragment != null) {
            Fragment fragmentById = fragmentManager.findFragmentById(R.id.landscapeLayout);
            transaction.replace(R.id.landscapeLayout, secondFragment, fragmentTwoTag);
        }

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        Log.i("MA", "fragment loaded, tablet = " + tablet + ", landscape = " + landscape);
        currentAction = action;
    }

    @Override
    public void onDisplayTrainsInClass(String classNum) {
        // Handler for click events from the class list
        Map args = new HashMap();
        args.put(CLASS_NUM_KEY, classNum);
        if (landscape) {
            if (!(secondFragment instanceof TrainListFragment)) {
                //   secondFragment = new TrainListFragment();
            } else {
                // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
                ((TrainListFragment) secondFragment).setShowTrainsForClass(classNum);
                ((TrainListFragment) secondFragment).reloadTrainList();
            }
        } else {
            loadFragment(TRAIN_LIST, true, args);
        }
        TrainspotterSharedPreferences.setClass(classNum);
        //View view = findViewById(R.id.trainListFragmentHolder);
    }

    // Handler for interaction with TrainListFragment
    @Override
    public void onShowTrainDetails(String classNum, String trainNum) {
        // TODO : depending on layout, if Tablet, left pane = train list, right = train details
        // TODO : if phone, load in train details fragment
        Map args = new HashMap();
        args.put(CLASS_NUM_KEY, classNum);
        args.put(TRAIN_NUM_KEY, trainNum);
        if (landscape) {
            if (secondFragment instanceof TrainDetailFragment) {
                // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
                ((TrainDetailFragment) secondFragment).resetData();
                ((TrainDetailFragment) secondFragment).setShowDetailsForTrain(classNum, trainNum);
                ((TrainDetailFragment) secondFragment).fetchTrainData();
                //((TrainDetailFragment)secondFragment).reloadTrainList();
            } else {
                loadFragment(TRAIN_LIST, true, args);
                // Now display
                if (secondFragment instanceof TrainDetailFragment) {
                    ((TrainDetailFragment) secondFragment).setShowDetailsForTrain(classNum, trainNum);
                }
            }
        } else {
            loadFragment(TRAIN_DETAIL, true, args);
        }
        TrainspotterSharedPreferences.setTrain(classNum, trainNum);
    }

    @Override
    public void onAddSightingForTrain(String classNum, String trainNum) {
        // Called if we click to add a sighting when we're displaying a trains details
        doLogSpot(classNum, trainNum);
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
        // Initialise the floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogSpot();
            }
        });
    }

    public void doLogSpot(String trainClass, String trainNum) {
        // Method to launch the "Log sighting" activity
        Intent intent = new Intent(this, LogSpotActivity.class);
        if (trainClass != null)
            intent.putExtra(CLASS_NUM_KEY, trainClass);
        if (trainNum != null)
            intent.putExtra(TRAIN_NUM_KEY, trainNum);

        startActivity(intent);
    }

    public void doLogSpot() {
        // Method called from main activity, checks if we're showing a trains details. If so, open "Log sighting"
        // with the train number completed
        String trainClass = null;
        String trainNum = null;
        if (currentAction == TRAIN_DETAIL) {
            // we're showing a train? If so, add log for that one
            if (fragment instanceof TrainDetailFragment) {
                TrainDetail currentTrain = ((TrainDetailFragment) fragment).getCurrentTrain();
                if (currentTrain != null && currentTrain.getTrain() != null) {
                    trainClass = currentTrain.getTrain().getClass_();
                    trainNum = currentTrain.getTrain().getNumber();
                }
            }
        }

        doLogSpot(trainClass, trainNum);
    }

    public void doLogSpot(String imagePath) {
        if (imagePath == null || imagePath.length() == 0)
            return;
        // We've taken a picture, so add a log for it!
        String trainClass = null;
        String trainNum = null;
        if (currentAction == TRAIN_DETAIL) {
            // we're showing a train? If so, add log for that one
            if (fragment instanceof TrainDetailFragment) {
                TrainDetail currentTrain = ((TrainDetailFragment) fragment).getCurrentTrain();
                if (currentTrain != null && currentTrain.getTrain() != null) {
                    trainClass = currentTrain.getTrain().getClass_();
                    trainNum = currentTrain.getTrain().getNumber();
                }
            }
        }

        Intent intent = new Intent(this, LogSpotActivity.class);
        if (trainClass != null)
            intent.putExtra(CLASS_NUM_KEY, trainClass);
        if (trainNum != null)
            intent.putExtra(TRAIN_NUM_KEY, trainNum);
        intent.putExtra(Constant.IMAGES_KEY, imagePath);

        startActivity(intent);
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
                }
            }
        }
    }

    // Camera callback
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_MAIN && resultCode == RESULT_OK) {
            doLogSpot(mCamera.getFilename());
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            // Image picked!
            Uri selectedImage = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            doLogSpot(picturePath);
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
