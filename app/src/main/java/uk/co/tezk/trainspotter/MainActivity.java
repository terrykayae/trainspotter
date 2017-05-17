package uk.co.tezk.trainspotter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.realm.Realm;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.view.ClassListFragment;
import uk.co.tezk.trainspotter.view.LogSpotFragment;
import uk.co.tezk.trainspotter.view.TrainDetailFragment;
import uk.co.tezk.trainspotter.view.TrainListFragment;

import static android.R.drawable;
import static uk.co.tezk.trainspotter.Utilitity.isLandscape;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.CLASS_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INITIALISING;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INVALID;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.LOG_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_DETAIL;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.PICK_IMAGE_FROM_GALLERY;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE;
import static uk.co.tezk.trainspotter.model.Constant.SHOW_CLASS;
import static uk.co.tezk.trainspotter.model.Constant.SHOW_ENGINE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ClassListFragment.OnClassListFragmentInteractionListener,
        TrainListFragment.OnTrainListFragmentInteractionListener,
        LogSpotFragment.OnFragmentInteractionListener,
        TrainDetailFragment.OnTrainDetailFragmentInteractionListener

{

    FloatingActionButton fab;
    CURRENT_ACTION currentAction;

    private Context context;
    // Store actions! If user pressed back, calling popBackStack() returns to previous fragment, but we don't know what activity that is
    // Store last activity here before updating
    private Stack<CURRENT_ACTION> actionStack = new Stack();
    // Set when views are loaded to determine layout
    private boolean landscape;
    private boolean tablet;

    Fragment fragment;
    // If we're loading two fragments, this is what and where for the second
    Fragment secondFragment;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Close any progress dialogues and fade the background
        doneInitialising();
        if (currentAction == INITIALISING)
            currentAction = CLASS_LIST;

        loadFragment(currentAction, false, null);

        // Load fragment, but don't add to the backstack - if we did on every onResume, we get a stack full!
        Log.i("MA", "onResume");
        // TODO : Store "null" in onRestoreInstance = will be details of second view in multipane layout

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
                    currentAction = actionStack.peek();
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
          //  startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

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
        findViewById(R.id.ivMallard).setAlpha(0.3f);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save currentAction
        outState.putInt(CURRENT_ACTION_KEY, CURRENT_ACTION.toInteger(currentAction));
        super.onSaveInstanceState(outState);
    }


    private void loadFragment(CURRENT_ACTION action, boolean addToBackStack, Map<String, String> params) {
        //Reset the fragment holders
        fragment = null;
        secondFragment = null;
        int secondViewId = 0;
        // Check for multi fragment layout
        landscape = isLandscape(this);
        if (findViewById(R.id.classListLandscapeLayout) != null || findViewById(R.id.trainListLandscapeLayout) != null) {
            // Landscape view found, load fragment
            tablet = true;
        }

        // TODO : Find more elegant way of finding whether we're tablet

        Log.i("MA", "Loading fragment " + action + " with addToBackStack " + addToBackStack);
        //setFabSpot();

        switch (action) {
            case CLASS_LIST:
                // If main fragment is already ClassListFragment, don't load again (Happens when Landscape and a class is clicked)
                fragment = new ClassListFragment();

                if (landscape) {
                    // We're landscape so need to load train list as well
                    secondViewId = R.id.trainListFragmentHolder;
                    TrainListFragment mTrainlistFragment = new TrainListFragment();
                    String classToShow = (params == null ? "1" : params.get(Constant.SHOW_CLASS));
                    Log.i("MA", "loadFragment, showing class " + classToShow);
                    mTrainlistFragment.setShowTrainsForClass(classToShow);
                    secondFragment = mTrainlistFragment;
                }

                break;
            case LOG_SPOT:
                setFabSave();
                fragment = new LogSpotFragment();
                if (params != null) {
                    ((LogSpotFragment) fragment).setTrainClass(params.get(Constant.CLASS_NUM_KEY));
                    ((LogSpotFragment) fragment).setTrainNum(params.get(Constant.TRAIN_NUM_KEY));
                }
                break;
            case TRAIN_LIST:
                TrainListFragment mTrainListFragment = new TrainListFragment();
                fragment = mTrainListFragment;
                String classToShow = (params == null ? "1" : params.get(Constant.SHOW_CLASS));
                Log.i("MA", "loadFragment, (port) showing class " + classToShow);
                mTrainListFragment.setShowTrainsForClass(classToShow);
                if (landscape) {
                    // If we're landscape, the train list goes on the right, class list on the left
                    TrainDetailFragment mTrainDetailFragment = new TrainDetailFragment();
                    String trainToShow = (params == null ? "1" : params.get(Constant.SHOW_ENGINE));
                    mTrainDetailFragment.setShowDetailsForTrain(classToShow, trainToShow);

                    secondFragment = mTrainDetailFragment;

                    secondViewId = R.id.trainDetailFragmentHolder;
                }
                break;
            case TRAIN_DETAIL:
                // if landscape, train details on the right, train list on left
                if (landscape) {
                    TrainListFragment mTrainListFragmentForDetail = new TrainListFragment();
                    fragment = mTrainListFragmentForDetail;
                    String classToShowForDetail = (params == null ? "1" : params.get(Constant.SHOW_CLASS));
                    Log.i("MA", "loadFragment, (land, traininfo) showing class " + classToShowForDetail);
                    mTrainListFragmentForDetail.setShowTrainsForClass(classToShowForDetail);
                } else {
                    fragment = new TrainDetailFragment();
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

        transaction.replace(R.id.mainContainer, fragment);
        if (secondFragment != null) {
            transaction.replace(secondViewId, secondFragment);
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
        if (action == CLASS_LIST) {
            if (findViewById(R.id.classListLandscapeLayout) != null) {
                // Landscape class list! Load Train list fragment into holder
                tablet = true;
                landscape = true;
            }
        } else if (action == TRAIN_LIST) {
            if (findViewById(R.id.trainListLandscapeLayout) != null) {
                // Landscape train list! Load train details fragment into holder
                tablet = true;
                landscape = true;
            }
        }

        currentAction = action;
    }

    @Override
    public void onDisplayTrainsInClass(String classNum) {
        // Handler for click events from the class list
        Map args = new HashMap();
        args.put(SHOW_CLASS, classNum);
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

    @Override // onSpotLog interaction
    public void onFragmentInteraction(Uri uri) {

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
                        params.put(Constant.CLASS_NUM_KEY, currentTrain.getTrain().getClass_());
                        params.put(Constant.TRAIN_NUM_KEY, currentTrain.getTrain().getNumber());
                    }
                }
                loadFragment(CURRENT_ACTION.LOG_SPOT, true, params);
            }
        });
    }

    private void setFabCamera() {
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_camera));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Camera!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFabSave() {
        // When the Add Sighting fragment is shown, FAB should save
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_save));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(context, "Save!", Toast.LENGTH_SHORT).show();
                if (fragment instanceof LogSpotFragment) {
                    Log.i("MA", "Saving!");
                    LogSpotFragment logSpotFragment = (LogSpotFragment) fragment;
                    logSpotFragment.handleSave();
                    onBackPressed();
                } else {
                    Log.i("MA", "Save pressed on none savabble screen?");
                }
            }
        });
    }

    // Handler for interaction with TrainListFragment
    @Override
    public void onShowTrainDetails(String classNum, String trainNum) {
        // TODO : depending on layout, if Tablet, left pane = train list, right = train details
        // TODO : if phone, load in train details fragment
        Log.i("MA", "onShowTrainDetails " + classNum + ", " + trainNum);
        Map args = new HashMap();
        args.put(SHOW_CLASS, classNum);
        args.put(SHOW_ENGINE, trainNum);
        if (landscape) {
            // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
            //    ((TrainListFragment)secondFragment).setShowTrainsForClass(classNum);
            //    ((TrainListFragment)secondFragment).reloadTrainList();
        } else {
            currentAction = TRAIN_DETAIL;
            loadFragment(currentAction, true, args);
        }
        Log.i("MA", "display trains in class " + classNum);
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
                        if (fragment instanceof LogSpotFragment) {
                            ((LogSpotFragment) fragment).getGoogleMap().setMyLocationEnabled(true);
                        }
                    }
                    break;
                    case MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS: {
                        if (fragment instanceof TrainDetailFragment) {
                            ((TrainDetailFragment) fragment).getGoogleMap().setMyLocationEnabled(true);
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
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
}
