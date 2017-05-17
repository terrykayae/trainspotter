package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.model.MyLocation;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.network.Submitter;
import uk.co.tezk.trainspotter.presenter.ILocationUpdatePresenter;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static uk.co.tezk.trainspotter.model.Constant.FRAG_TAG_DATE_PICKER;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogSpotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LogSpotFragment extends Fragment implements
        OnMapReadyCallback,
        ILocationUpdatePresenter.IView {
    private Context context;
    private OnFragmentInteractionListener mListener;

    SupportMapFragment mSupportMapFragment;
    private GoogleMap googleMap;

    @Inject MyLocation locationPresenter;

    // Include getter for googlemap so mainActivity can enable location if needed after permission check
    public GoogleMap getGoogleMap() { return googleMap; }

    public LogSpotFragment() {
        // Required empty public constructor
    }

    // The view items
    @BindView(R.id.etTrainId)
    EditText etTrainId;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.mapHolder)
    FrameLayout mapHolder;

    // Holder for the location, if we're allowed to fetch
    LatLng mLatLng;

    String trainClass;
    String trainNum;

    public void setTrainClass(String trainClass) {
        this.trainClass = trainClass;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initiate Dagger injections
        TrainSpotterApplication.getApplication().getLocationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_spot, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialise Butterknife
        ButterKnife.bind(this, view);
        // Set the date textview to today's date
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        //Ask for our location
        locationPresenter.bind(this);
        locationPresenter.getLocation();
        if (trainNum!=null) {
            etTrainId.setText(trainNum);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mSupportMapFragment = SupportMapFragment.newInstance();
        transaction.replace(R.id.mapHolder, mSupportMapFragment);
        mSupportMapFragment.getMapAsync(this);
        transaction.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.detach(mSupportMapFragment);
        // transaction.detach(this);

        transaction.commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.i("LSF", "Got map");
        // Enable the setMyLocation
        try {
            if (Utilitity.checkLocationPermissions(context, MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT)) {
                googleMap.setMyLocationEnabled(true);
                if (mLatLng != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Called by the location presenter to provide our lat and long
    @Override
    public void updatePosition(LatLng latLng) {
        if (mLatLng == null && googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        this.mLatLng = latLng;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    public void handleSave() {
        // Called when FAB is pressed in main activity and we're active
        if (tvDate.getText().length() == 0 || etTrainId.getText().length() == 0) {
            Toast.makeText(getContext(), "Fields were blank!\nNot saving", Toast.LENGTH_LONG).show();
            return;
        }
        // TODO : Save location as well
        // Create and store object
        SightingDetails sightingDetails = new SightingDetails();
        sightingDetails.setTrainId(etTrainId.getText().toString());
        sightingDetails.setDate(tvDate.getText().toString());
        // We received a location, or one was set by user
        if (mLatLng!=null) {
            sightingDetails.setLat((float)mLatLng.latitude);
            sightingDetails.setLon((float)mLatLng.longitude);

        }
        // Store in the DB
        RealmHandler.getInstance().persistSightingDetails(sightingDetails);
        // Send to the server
        Submitter.getInstance().submitSighting(sightingDetails);
    }

    @OnClick(R.id.tvDate)
    void showCalendar() {
        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                        tvDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                })
                .setFirstDayOfWeek(Calendar.SUNDAY);
        cdp.show(getActivity().getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
    }
}
