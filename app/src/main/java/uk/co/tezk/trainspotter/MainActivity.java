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

import java.util.Stack;

import uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION;
import uk.co.tezk.trainspotter.view.ClassListFragment;
import uk.co.tezk.trainspotter.view.LogSpotFragment;
import uk.co.tezk.trainspotter.view.TrainDetailFragment;
import uk.co.tezk.trainspotter.view.TrainListFragment;
import uk.co.tezk.trainspotter.view.dummy.DummyContent;

import static android.R.drawable;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.CLASS_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INITIALISING;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.INVALID;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.LOG_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION.TRAIN_LIST;
import static uk.co.tezk.trainspotter.model.Constant.CURRENT_ACTION_KEY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ClassListFragment.OnClassListFragmentInteractionListener,
        LogSpotFragment.OnFragmentInteractionListener

{
    FloatingActionButton fab;
    CURRENT_ACTION currentAction;

    private Context context;
    // Store actions! If user pressed back, calling popBackStack() returns to previous fragment, but we don't know what activity that is
    // Store last activity here before updating
    private Stack<CURRENT_ACTION>actionStack = new Stack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentAction = INITIALISING;
        context = this;
        // Initalise the action stack - we don't push the current state from onResume, so need first element to be CLASS_LIST
        if (actionStack.size()==0)
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

        if (savedInstanceState!=null) {
            if (savedInstanceState.getInt(CURRENT_ACTION_KEY)>0) {
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
        doneInitialising();
        if (currentAction == INITIALISING) {
            currentAction = CLASS_LIST;
        }
        // Load fragment, but don't add to the backstack
        Log.i("MA", "onResume");
        loadFragment(currentAction, false);
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
                if (actionStack.size()>0) {
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

    private void loadFragment(CURRENT_ACTION action, boolean addToBackStack) {
        Fragment fragment=null;
        Log.i("MA", "Loading fragment "+action+" with addToBackStack "+addToBackStack);
        //setFabSpot();
        switch (action) {
            case CLASS_LIST:
                fragment = new ClassListFragment();
                break;
            case LOG_SPOT:
                setFabCamera();
                fragment = new LogSpotFragment();
                break;
            case TRAIN_LIST:
                fragment = new TrainListFragment();
                break;
            case TRAIN_INFO:
                fragment = new TrainDetailFragment();
                break;
            default :
                fragment = new ClassListFragment();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setEnterTransition(new Explode());
            fragment.setReturnTransition(new Explode());
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
            actionStack.push(action);
        }
        transaction.commit();

        // Now that the fragment has loaded, check for the presence of Landscape view holders which indicate we're
        // on a tablet
        if (action == CLASS_LIST) {
            if (findViewById(R.id.classListLandscapeLayout)!=null) {
                // Landscape class list! Load Train list fragment into holder
            }
        } else if (action == TRAIN_LIST) {
            if (findViewById(R.id.trainListLandscapeLayout)!=null) {
                // Landscape train list! Load train details fragment into holder
            }
        }

        currentAction = action;

    }


    @Override // onSpotLog interaction
    public void onFragmentInteraction(Uri uri) {

    }

    // Deal with fragment communication
    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    private void setFabSpot() {
        fab.setImageDrawable(getResources().getDrawable(drawable.ic_menu_add));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load the "Spotting" fragment, save on the backstack
                loadFragment(CURRENT_ACTION.LOG_SPOT, true);
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
}
