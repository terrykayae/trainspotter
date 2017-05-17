package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.adapters.GalleryRecyclerViewAdapter;
import uk.co.tezk.trainspotter.model.MyLocation;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.network.Submitter;
import uk.co.tezk.trainspotter.parcel.MapViewParcelable;
import uk.co.tezk.trainspotter.presenter.ILocationUpdatePresenter;
import uk.co.tezk.trainspotter.realm.RealmHandler;

import static uk.co.tezk.trainspotter.model.Constant.DATE_RECORDED_KEY;
import static uk.co.tezk.trainspotter.model.Constant.FRAG_TAG_DATE_PICKER;
import static uk.co.tezk.trainspotter.model.Constant.MAP_VIEW_PARCELABLE_KEY;
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
        ILocationUpdatePresenter.IView,
        GalleryRecyclerViewAdapter.OnImageClickListener {
    private Context context;

    SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;

    private String imageFilename;
    private List<String>imageList;

    @Inject
    MyLocation locationPresenter;

    //Holders for settings that might be passed in through onCreate that will be needed later
    MapViewParcelable mapSettings;

    // Include getter for googlemap so mainActivity can enable location if needed after permission check
    public GoogleMap getmGoogleMap() {
        return mGoogleMap;
    }

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
    @BindView(R.id.rvSpotImages)
    RecyclerView rvGallery;

    // Holder for the location, if we're allowed to fetch
    LatLng mLatLng;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("LSF","onCreate");
        // Clear settings
        //mapSettings = null;
        // Initiate Dagger injections
        TrainSpotterApplication.getApplication().getLocationComponent().inject(this);

        //Ask for our location
        locationPresenter.bind(this);
        locationPresenter.getLocation();
        if (trainNum != null) {
            etTrainId.setText(trainNum);
        }

        if (galleryRecyclerViewAdapter == null)
            galleryRecyclerViewAdapter = new GalleryRecyclerViewAdapter(new ArrayList(), getContext(), this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore mapSettings
        if (savedInstanceState != null) {
            mapSettings = savedInstanceState.getParcelable(MAP_VIEW_PARCELABLE_KEY);
            if (mGoogleMap != null && mapSettings != null) {
                Log.i("LSF","Calling onMapReady() from oVSR");
                onMapReady(mGoogleMap);
            }
            Log.i("LSF", "onVSR() mapSettings = "+mapSettings);
        } else {
            Log.i("LSF", "onVSR() no map settings");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_spot, container, false);

        FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.mapHolder, mSupportMapFragment);
        mSupportMapFragment.getMapAsync(this);
        transaction.commit();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialise Butterknife
        ButterKnife.bind(this, view);
        // Set the date textview to today's date
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        //Initialise the gallery
        rvGallery.setAdapter(galleryRecyclerViewAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvGallery.setLayoutManager(layoutManager);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        mSupportMapFragment = SupportMapFragment.newInstance();

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
        // FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        //   FragmentTransaction transaction = fragmentManager.beginTransaction();
        //   transaction.remove(mSupportMapFragment);
        //transaction.detach(mSupportMapFragment);
        //    transaction.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
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
        // restore settings if possible
        if (mapSettings != null) {
            CameraPosition cameraPosition = new CameraPosition(
                    new LatLng(mapSettings.getLat(), mapSettings.getLon()),
                    mapSettings.getZoomLevel(),
                    mapSettings.getTilt(),
                    mapSettings.getBearing()
            );
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(cameraUpdate);
            Log.i("LSF", "Moved camera due to savedSettings in onMapReady()");
        }
    }

    // Called by the location presenter to provide our lat and long, if we've set from settings, don't move camera. just store
    @Override
    public void updatePosition(LatLng latLng) {
        if (mapSettings==null)
            if (mLatLng == null && mGoogleMap != null) {
                Log.i("LSF", "Moving camera due to position update");
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        this.mLatLng = latLng;
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
        if (mLatLng != null) {
            sightingDetails.setLat((float) mLatLng.latitude);
            sightingDetails.setLon((float) mLatLng.longitude);

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

    // Received image click = url is url of image clicked, either filename or TAKE_PHOTO to take a photo
    @Override
    public void onClick(String imageUrl) {
        if (TAKE_PHOTO.equals(imageUrl)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File imageFile = null;
                try {
                    imageFile = Utilitity.createImageFile(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (imageFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(
                            context,
                            "uk.co.tezk.trainspotter",
                            imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    imageFilename = imageFile.getAbsolutePath();

                    Log.i("LSF","Image will save at : "+imageFile.getAbsolutePath());
                }

                ((MainActivity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_FROM_SPOT);
            }
        } else {
            // TODO : Show bigger image
            Toast.makeText(context, imageUrl, Toast.LENGTH_SHORT).show();
        }
    }

    // Called by MainAcivity when it receives notification that our image is ready
    public void onImageReady() {
        Log.i("LSF", "image "+imageFilename+" is ready for access");
        // Notify the adapter so it can display the image
        GalleryRecyclerViewAdapter adapter = ((GalleryRecyclerViewAdapter) rvGallery.getAdapter());
        adapter.addImage(imageFilename);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        adapter.notifyDataSetChanged();
        // Save the filename to our list
        imageList.add(imageFilename);
    }
}
