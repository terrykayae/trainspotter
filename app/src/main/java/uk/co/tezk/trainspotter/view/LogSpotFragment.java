package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.geocode.GeocodePresenter;
import uk.co.tezk.trainspotter.utility.Utilitity;
import uk.co.tezk.trainspotter.adapters.GalleryRecyclerViewAdapter;
import uk.co.tezk.trainspotter.base.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.Camera;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.MyLocation;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.network.Submitter;
import uk.co.tezk.trainspotter.model.parcel.MapViewParcelable;
import uk.co.tezk.trainspotter.geocode.GeocodeContract;
import uk.co.tezk.trainspotter.presenter.LocationUpdateContract;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static uk.co.tezk.trainspotter.model.Constant.DATE_RECORDED_KEY;
import static uk.co.tezk.trainspotter.model.Constant.FRAG_TAG_DATE_PICKER;
import static uk.co.tezk.trainspotter.model.Constant.IMAGES_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MAP_VIEW_PARCELABLE_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.REQUEST_IMAGE_CAPTURE_FROM_SPOT;
import static uk.co.tezk.trainspotter.model.Constant.TAKE_PHOTO;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_NUM_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class LogSpotFragment extends Fragment implements
        OnMapReadyCallback,
        LocationUpdateContract.View,
        GeocodeContract.View,
        GalleryRecyclerViewAdapter.OnImageClickListener {
    private Context context;

    // Holder to access progressDialog in MainActivity
    private TrainspotterDialogSupport progressDialog;

    SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;

    private List<String>imageList;

    // Camera helper - deals with permissions for us, static so we retain reference
    private static Camera mCamera;
    // Allow mainActivity to set our camera if they receive the clickMessage
    public void setCamera(Camera camera) { this.mCamera = camera; }

    @Inject
    MyLocation locationPresenter;

    GeocodeContract.Presenter geocodePresenter;

    //Holders for settings that might be passed in that will be needed later
    MapViewParcelable mapSettings;

    // Include getter for googlemap so mainActivity can enable location for us if needed after permission check
    public GoogleMap getmGoogleMap() {
        return mGoogleMap;
    }

    public LogSpotFragment() {
        // Required empty public constructor
    }

    // The view items
    @BindView(R.id.etTrainId) EditText etTrainId;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.mapHolder) FrameLayout mapHolder;
    @BindView(R.id.rvSpotImages) RecyclerView rvGallery;
    @BindView(R.id.etLocation) EditText etLocation;

    // Holder for the location, if we're allowed to fetch
    LatLng mLatLng;
    // Adapter for the gallery of images
    GalleryRecyclerViewAdapter galleryRecyclerViewAdapter;

    String trainClass;
    String trainNum;

    public void setTrainClass(String trainClass) {
        this.trainClass = trainClass;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        mSupportMapFragment = SupportMapFragment.newInstance();
        if (context instanceof TrainspotterDialogSupport)
            progressDialog = (TrainspotterDialogSupport)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initiate Dagger injections
        TrainSpotterApplication.getApplication().getLocationComponent().inject(this);

        //Ask for our location (Dagger injected)
        locationPresenter.bind(this);
        locationPresenter.getLocation();

        //attachListener the Geocoder
        geocodePresenter = new GeocodePresenter();
        geocodePresenter.bind(this);

        //initialise the gallery adapter
        List <String> images = new ArrayList();
        if (mCamera!=null) {
            String imageFilename = mCamera.getFilename();
            Log.i("LSF", "onCreate, imageFilename = " + imageFilename);
            if (imageFilename != null)
                images.add(imageFilename);
        }
        galleryRecyclerViewAdapter = new GalleryRecyclerViewAdapter(images, getContext(), this, false);
        imageList = images;
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        android.view.View view = inflater.inflate(R.layout.fragment_log_spot, container, false);

        // Initialise Butterknife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (trainNum != null) {
            etTrainId.setText(trainNum);
        }
        // Set the date textview to today's date
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        //Initialise the gallery
        rvGallery.setAdapter(galleryRecyclerViewAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvGallery.setLayoutManager(layoutManager);
        if (rvGallery.getAdapter().getItemCount() > 0) {
            rvGallery.setVisibility(android.view.View.VISIBLE);
        } else {
            rvGallery.setVisibility(android.view.View.GONE);
        }

        FragmentManager fragmentManager = getChildFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.mapHolder, mSupportMapFragment);
        mSupportMapFragment.getMapAsync(this);
        transaction.commit();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore mapSettings
            mapSettings = savedInstanceState.getParcelable(MAP_VIEW_PARCELABLE_KEY);
            if (mGoogleMap != null && mapSettings != null) {
                onMapReady(mGoogleMap);
            }
            // Restore the UI
            if (savedInstanceState.getString(TRAIN_NUM_KEY) != null)
                etTrainId.setText(savedInstanceState.getString(TRAIN_NUM_KEY));
            if (savedInstanceState.getString(DATE_RECORDED_KEY) != null)
                tvDate.setText(savedInstanceState.getString(DATE_RECORDED_KEY));
            // And the images
            if (savedInstanceState.getStringArrayList(IMAGES_KEY) != null) {
                imageList = savedInstanceState.getStringArrayList(IMAGES_KEY);
                for (String each : imageList)
                    galleryRecyclerViewAdapter.addImageFromFile(each);
            }
        }
    }

    public void onResume() {
        super.onResume();
        locationPresenter.bind(this);
    }

    public void onPause() {
        super.onPause();
        locationPresenter.unBind();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        geocodePresenter.unBind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the map position
        CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
        MapViewParcelable mapSettings = new MapViewParcelable(
                cameraPosition.target.latitude,
                cameraPosition.target.longitude,
                cameraPosition.zoom,
                cameraPosition.bearing,
                cameraPosition.tilt
        );
        outState.putParcelable(MAP_VIEW_PARCELABLE_KEY, mapSettings);

        //Save the date and train number
        outState.putString(TRAIN_NUM_KEY, etTrainId.getText().toString());
        outState.putString(DATE_RECORDED_KEY, tvDate.getText().toString());

        // Save the images
        if (imageList !=null && imageList.size()>0) {
            outState.putStringArrayList(IMAGES_KEY, (ArrayList)imageList);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
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
        // restore settings if possible
        if (mapSettings != null) {
            CameraPosition cameraPosition = new CameraPosition(
                    new LatLng(mapSettings.getLat(), mapSettings.getLon()),
                    mapSettings.getZoomLevel(),
                    mapSettings.getTilt(),
                    mapSettings.getBearing()
            );
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    // Called by the location presenter to provide our lat and long, if we've already moved from settings, don't move camera. just store
    @Override
    public void updatePosition(LatLng latLng) {
        if (mapSettings==null)
            if (mLatLng == null && mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        this.mLatLng = latLng;

        // Now try and get the geocoded location
        geocodePresenter.getLocation((float)latLng.latitude, (float)latLng.longitude, Constant.GEOCODER_API_KEY);
    }

    // Called when FAB is pressed in main activity and we're active - return true if we've saved
    public boolean handleSave() {
        if (tvDate.getText().length() == 0 || etTrainId.getText().length() == 0) {
            Toast.makeText(getContext(), getResources().getText(R.string.missing_train_number_error), Toast.LENGTH_LONG).show();
            return false;
        }
        // Create and store object
        SightingDetails sightingDetails = new SightingDetails();
        sightingDetails.setTrainId(etTrainId.getText().toString());
        // TODO : // FIXME
        sightingDetails.setTrainClass(trainClass==null?"0":trainClass);
        sightingDetails.setLocationName(etLocation.getText().toString());
        sightingDetails.setDate(tvDate.getText().toString());
        sightingDetails.setTime(Utilitity.getTime(new Date()));
        // We received a location, or one was set by user
        if (mLatLng != null) {
            sightingDetails.setLat((float) mLatLng.latitude);
            sightingDetails.setLon((float) mLatLng.longitude);
        }
        // Save the images
        RealmHandler.getInstance().persistImageDetails(imageList, sightingDetails);
        // Store in the DB
        RealmHandler.getInstance().persistSightingDetails(sightingDetails);
        // Send to the server
        Submitter.getInstance().submitSighting(sightingDetails);
        return true;
    }

    // Handler for the date textfield = show a calendar and update
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

    // Received image click = url is url of image clicked, either filename or TAKE_PHOTO to take a photo
    @Override
    public void onClick(String imageUrl) {
        if (TAKE_PHOTO.equals(imageUrl)) {
            if (mCamera == null)
                mCamera = new Camera(context, REQUEST_IMAGE_CAPTURE_FROM_SPOT, MY_PERMISSIONS_REQUEST_EXTERNAL_WRITE_FROM_SPOT);
            // Start activity, when onImageReady is called, we can get filename from camera
            mCamera.takePicture();

        } else {
            // TODO : Show bigger image
            Toast.makeText(context, imageUrl, Toast.LENGTH_SHORT).show();
        }
    }

    // Called by MainActivity when it receives notification that our image is ready
    public void onImageReady() {
        String imageFilename = mCamera.getFilename();
        Log.i("LSF", "image ready : "+imageFilename);
        // Ask the camera to notify the gallery
        mCamera.addToGallery();
        // update and notify the gallery adapter so it can display the image
        GalleryRecyclerViewAdapter adapter = ((GalleryRecyclerViewAdapter) rvGallery.getAdapter());
        adapter.addImageFromFile(imageFilename);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        adapter.notifyDataSetChanged();
        // Save the filename to our list
        rvGallery.setVisibility(android.view.View.VISIBLE);
        imageList.add(imageFilename);
    }

    // If we're going to be showing an image
    public void setImage(String imageFilename) {
        mCamera = new Camera(getContext(), 0 ,0);
        mCamera.setFilename(imageFilename);
    }

    // Callback for our Geocode api
    @Override
    public void updateLocation(String details) {
        etLocation.setText(details);


    }

    public void clear() {
        trainClass = "";
        trainNum = "";
    }
}
