package uk.co.tezk.trainspotter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.google.android.gms.maps.SupportMapFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION;
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
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION_KEY;
import static uk.co.tezk.trainspotter.model.Constant.SHOW_CLASS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ClassListFragment.OnClassListFragmentInteractionListener,
        TrainListFragment.OnTrainListFragmentInteractionListener,
        LogSpotFragment.OnFragmentInteractionListener

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
    // If we're loading two fragments, this is what and were for the second
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
        if (currentAction == INITIALISING) {
            currentAction = CLASS_LIST;
        }
        // Load fragment, but don't add to the backstack - if we did on every onResume, we get a stack full!
        Log.i("MA", "onResume");
        // TODO : Store "null" in onRestoreInstance = will be details of second view in multipane layout
        loadFragment(currentAction, false, null);
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
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

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
        findViewById(R.id.tvLoading).setVisibility(View.GONE);
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
                if (!(fragment instanceof ClassListFragment))
                    fragment = new ClassListFragment();
                if (landscape) {
                    // We're landscape so need to load train list as well
                    secondViewId = R.id.trainListFragmentHolder;
                    secondFragment = new TrainListFragment();
                    String classToShow = (params==null?"1":params.get(Constant.SHOW_CLASS));
                    Log.i("MA", "loadFragment, showing class "+classToShow);
                    ((TrainListFragment)secondFragment).setShowTrainsForClass(classToShow);
                }

                break;
            case LOG_SPOT:
                setFabSave();
                fragment = new LogSpotFragment();
                break;
            case TRAIN_LIST:
                if (landscape) {
                    // If we're landscape, the train list goes on the right, class list on the left

                } else {
                    // Portrait, just show the TrainList
                    fragment = new TrainListFragment();

                    String classToShow = (params==null?"1":params.get(Constant.SHOW_CLASS));
                    Log.i("MA", "loadFragment, (port) showing class "+classToShow);
                    ((TrainListFragment)fragment).setShowTrainsForClass(classToShow);
                }
                break;
            case TRAIN_INFO:
                // if landscape, train details on the right, train list on left
                if (landscape) {

                } else {
                    fragment = new TrainDetailFragment();
                }
                break;
            default:
                fragment = new ClassListFragment();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setEnterTransition(new Explode());
            fragment.setReturnTransition(new Explode());
        }

        // Swap in the fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.mainContainer, fragment);
        if (secondFragment!=null) {
            transaction.replace(secondViewId, secondFragment);
        }

        // Do we need to load a map fragment into the view?
        if (currentAction == LOG_SPOT) {
                SupportMapFragment mSupportMapFragment = SupportMapFragment.newInstance();
                transaction.replace(R.id.mapHolder, mSupportMapFragment);
                mSupportMapFragment.getMapAsync((LogSpotFragment)fragment);
                // Add another LOG_SPOT to the actionStack as we'll be popping two fragments?
                actionStack.push(LOG_SPOT);
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
        // TODO : Determine whether we're tablet or phone - tablet show classes on left pane, trains on right
        // TODO : phone, load the new fragment
        Map args = new HashMap();
        args.put(SHOW_CLASS, classNum);
        if (landscape) {
            // Current action doesn't change as we still show class list in landscape, tell second fragment to load new class
            ((TrainListFragment)secondFragment).setShowTrainsForClass(classNum);
            ((TrainListFragment)secondFragment).reloadTrainList();
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

    private void setFabSpot() {
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load the "Spotting" fragment, save on the backstack
                loadFragment(CURRENT_ACTION.LOG_SPOT, true, null);
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
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_save));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(context, "Save!", Toast.LENGTH_SHORT).show();
                if (fragment instanceof LogSpotFragment) {
                    Log.i("MA", "Saving!");
                    LogSpotFragment logSpotFragment = (LogSpotFragment)fragment;
                    logSpotFragment.handleSave();
                    onBackPressed();
                } else {
                    Log.i("MA", "Save pressed on none savabble screen?");
                }
            }
        });
    }


    @Override
    public void onShowTrainDetails(String classNum, String trainNum) {
        // TODO : depending on layout, if Tablet, left pane = train list, right = train details
        // TODO : if phone, load in train details fragment
        Log.i("MA", "onShowTrainDetails " + classNum + ", " + trainNum);
    }
}
