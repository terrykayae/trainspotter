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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.adapters.GalleryRecyclerViewAdapter;
import uk.co.tezk.trainspotter.interfaces.TrainspotterDialogSupport;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.parcel.MapViewParcelable;
import uk.co.tezk.trainspotter.parcel.TrainParcel;
import uk.co.tezk.trainspotter.presenter.ITrainDetailPresenter;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenterImpl;

import static uk.co.tezk.trainspotter.model.Constant.MAP_VIEW_PARCELABLE_KEY;
import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS;

/**
 * A simple {@link Fragment} subclass.
 */

public class TrainDetailFragment extends Fragment implements
        OnMapReadyCallback,
        ITrainDetailPresenter.IView,
        GalleryRecyclerViewAdapter.OnImageClickListener {
    Context context;

    // Holder to access trainspotterDialog in MainActivity
    private TrainspotterDialogSupport trainspotterDialog;

    // Textview fields
    @BindView(R.id.tvClass) TextView tvClass;
    @BindView(R.id.tvTrainNumber) TextView tvTrainNumber;
    @BindView(R.id.tvTrainName) TextView tvTrainName;
    @BindView(R.id.tvTrainLivery) TextView tvTrainLivery;
    @BindView(R.id.tvTrainOperator) TextView tvTrainOperator;
    @BindView(R.id.tvTrainDepot) TextView tvTrainDepot;
    @BindView(R.id.tvTrainLastSpotted) TextView tvTrainLastSpotted;
    @BindView(R.id.tvTrainWhere) TextView tvTrainWhere;
    @BindView(R.id.rvGallery) RecyclerView rvGallery;
    // Holders for the map and associated items
    Map<String, Marker> markers;
    SupportMapFragment mSupportMapFragment;
    GoogleMap mGoogleMap;
    boolean mapReady = false;
    // Allow MainActivity to forceportrait so our createView will load in a Portrait layout if
    // we're a subfragment, otherwise android loads in the landscape fragment...
    public boolean forcePortrait = false;
    // Holder for a fileName that can be set efore createView to show an inital image in the gallery
    private String imageFilename;
    public void setImageFilename(String filename) { imageFilename = filename; }

    // Include getter for GoogleMap so mainActivity can enable location
    public GoogleMap getGoogleMap() {
        return mGoogleMap;
    }
    // Holder for current train
    private TrainDetail currentTrain;
    // filenames for the images
    private List<String>imageList;
    // Include getter so Activity can see what we're showing
    public TrainDetail getCurrentTrain() {
        return currentTrain;
    }
    //Our presenter
    ITrainDetailPresenter.IPresenter presenter;

    //Holders for settings that might be passed in that will be needed later
    MapViewParcelable mapSettings;
    // If we get sent a notification to display, store the item here and deal with in onDisplayDetails
    TrainParcel trainParcel;

    // Adapter for the gallery of images
    GalleryRecyclerViewAdapter galleryRecyclerViewAdapter;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTrainDetailFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAddSightingForTrain(String classNum, String trainNum);
    }

    private OnTrainDetailFragmentInteractionListener mListener;

    public TrainDetailFragment() {
        // Required empty public constructor
    }


    public void forcePortrait(boolean forcePortrait) {
        // As we use different layouts, set this to force potrait mode when in dual pane mode
        this.forcePortrait = forcePortrait;
    }

    public void setShowDetailsForTrain(String classNumber, String trainNumber) {
        // Set which train we'll be showing once loaded - called after creating object and before the
        // fragment is loaded into the view hierachy
        // if train == 1, get first for class
        if (currentTrain == null) {
            currentTrain = new TrainDetail();
            TrainListItem trainListItem = new TrainListItem();
            currentTrain.setTrain(trainListItem);
        }
        currentTrain.getTrain().setClass_(classNumber);
        currentTrain.getTrain().setNumber(trainNumber);

        Log.i("TDF", "setting currentTrain = " + currentTrain);
        Log.i("TDF", "class " + currentTrain.getTrain().getClass_());
        Log.i("TDF", "id " + currentTrain.getTrain().getNumber());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnTrainDetailFragmentInteractionListener) {
            mListener = (OnTrainDetailFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof TrainspotterDialogSupport)
            trainspotterDialog = (TrainspotterDialogSupport) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String classNum = savedInstanceState.getString(Constant.CLASS_NUM_KEY);
            String trainNum = savedInstanceState.getString(Constant.TRAIN_NUM_KEY);
            if (classNum != null && trainNum != null) {
                setShowDetailsForTrain(classNum, trainNum);
            }
        }
        //initialise the gallery adapter
        //if (galleryRecyclerViewAdapter == null)
        List <String> images = new ArrayList();
        Log.i("LSF", "onCreate, imageFilename = "+imageFilename);
        if (imageFilename!=null)
            images.add(imageFilename);
        galleryRecyclerViewAdapter = new GalleryRecyclerViewAdapter(images, getContext(), this, false);
        imageList = images;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int layoutId = forcePortrait?R.layout.fragment_train_detail_subfragment:R.layout.fragment_train_detail;
        View view = inflater.inflate(layoutId, container, false);
        // Butter knife bindings
        ButterKnife.bind(this, view);
        presenter = new TrainDetailPresenterImpl();
        presenter.bind(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentTrain != null)
            presenter.retrieveData(currentTrain.getTrain().getClass_(), currentTrain.getTrain().getNumber());
        // Load the map
        FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mSupportMapFragment = SupportMapFragment.newInstance();
        transaction.replace(R.id.mapHolder, mSupportMapFragment);
        mSupportMapFragment.getMapAsync(this);
        transaction.commit();

        //Initialise the gallery
        rvGallery.setAdapter(galleryRecyclerViewAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvGallery.setLayoutManager(layoutManager);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);




        if (savedInstanceState != null) {
            String classNum = savedInstanceState.getString(Constant.CLASS_NUM_KEY);
            String trainNum = savedInstanceState.getString(Constant.TRAIN_NUM_KEY);
            if (classNum != null && trainNum != null) {
                setShowDetailsForTrain(classNum, trainNum);
            }

            // Restore mapSettings
            mapSettings = savedInstanceState.getParcelable(MAP_VIEW_PARCELABLE_KEY);
            if (mGoogleMap != null && mapSettings != null) {
                onMapReady(mGoogleMap);
            }
        }
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

        super.onSaveInstanceState(outState);
        if (currentTrain != null) {
            outState.putString(Constant.CLASS_NUM_KEY, currentTrain.getTrain().getClass_());
            outState.putString(Constant.TRAIN_NUM_KEY, currentTrain.getTrain().getNumber());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unbind();
        presenter = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Called by Recyclerview when we click an image
    @Override
    public void onClick(String imageUrl) {
        Toast.makeText(getContext(), "Show "+imageUrl, Toast.LENGTH_SHORT).show();
    }

    // Called when map is ready and loaded
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mapReady = true;
        // Enable the setMyLocation, if we've got permission. If not this will askk
        try {
            if (Utilitity.checkLocationPermissions(context, MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS)) {
                googleMap.setMyLocationEnabled(true);
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
        // Display flags for any sightings!
        addSightingsToMap();
    }

    public void addSightingsToMap() {
        // Add markers for all of the sightings in the SightingDetail collection and
        // centre the map there
        markers = new HashMap();
        if (currentTrain.getSightings() != null && currentTrain.getSightings().size() > 0) {
            // Save the boundaries and average of the points
            int count = 0;
            double avgLat = 0;
            double avgLon = 0;
            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;

            // Add marker
            BitmapDescriptor flagIcon = BitmapDescriptorFactory.fromResource(R.drawable.green_flag);
            for (SightingDetails each : currentTrain.getSightings()) {
                Log.i("TDF", "Sighting at : " + each.getLat() + ", " + each.getLon());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(each.getLat(), each.getLon()))
                        .title(each.getDate())
                        .icon(flagIcon)
                        .anchor(0.2f, 1f);

                markers.put(each.getTrainId(), mGoogleMap.addMarker(markerOptions));

                // Map boundaries
                if (each.getLat() > maxLat)
                    maxLat = each.getLat();
                if (each.getLat() < minLat)
                    minLat = each.getLat();
                if (each.getLon() > maxLon)
                    maxLon = each.getLon();
                if (each.getLon() < minLon)
                    minLon = each.getLon();
                avgLat += each.getLat();
                avgLon += each.getLon();
                if (count++ > 0) {
                    avgLat /= 2;
                    avgLon /= 2;
                }
                // latlon, zoom, tilt, bearin
                //CameraPosition cameraPosition = new CameraPosition(new LatLng(avgLat, avgLon), 0, 0, 0);
                //CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                // 1st sw, 2nd = ne
                // Lots of sightings? Centre around them
                if (currentTrain.getSightings().size() > 1) {
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int setAt = Math.min(width, height);

                    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(minLat, minLon), new LatLng(maxLat, maxLon));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100); // 100 = padding
                    mGoogleMap.animateCamera(cameraUpdate);
                } else {
                    // Just one sighting, zoom to average
                    CameraPosition cameraPosition = CameraPosition.builder()
                            .target(new LatLng(avgLat, avgLon))
                            .zoom(14f)
                            .build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }
    }

    // Called by our presenter
    @Override
    public void showTrainDetails(TrainDetail trainDetail) {
        Log.i("TDF", "showTrainDetails " + trainDetail);
        this.currentTrain = trainDetail;
        tvClass.setText(trainDetail.getTrain().getClass_());
        tvTrainNumber.setText(trainDetail.getTrain().getNumber());
        tvTrainName.setText(trainDetail.getTrain().getName());
        tvTrainLivery.setText(trainDetail.getTrain().getLivery());
        tvTrainOperator.setText(trainDetail.getTrain().getOperator());
        tvTrainDepot.setText(trainDetail.getTrain().getDepot());
        if (trainParcel != null) {
            // We're showing a notification, remove the sightings and add our own
            List<SightingDetails> newSightings = new ArrayList();
            SightingDetails sighting = new SightingDetails("Just now", trainParcel.getLat(), trainParcel.getLon());

            newSightings.add(sighting);
            trainDetail.setSightings(newSightings);
        } else {
            //We don't want to show images for notifications, so only add here
            Log.i("TDF", "Images = train : "+trainDetail.getImages());


        }
        if (trainDetail.getSightings() == null || trainDetail.getSightings().size() == 0) {
            tvTrainLastSpotted.setText(getText(R.string.none_reported));
        } else {
            SightingDetails latestSighting = trainDetail.getSightings().get(0);
            for (SightingDetails each : trainDetail.getSightings()) {
                if (each.getDate().compareTo(latestSighting.getDate()) > 0) {
                    latestSighting = each;
                }
            }
            tvTrainLastSpotted.setText(latestSighting.getDate());
            if (latestSighting.getLocationName() != null && latestSighting.getLocationName().length() > 0) {
                tvTrainWhere.setText(latestSighting.getLocationName());
            } else {
                tvTrainWhere.setText(latestSighting.getLat() + ", " + latestSighting.getLon());
            }
        }


        // If the map was already loaded, add the markers
        if (mapReady)
            addSightingsToMap();
    }

    // handle interaction from Presenter

    @Override
    public void onStartLoading() {
        Log.i("TDF", "onStartLoading");
        trainspotterDialog.startProgressDialog();
    }

    @Override
    public void onErrorLoading(String message) {
        Log.i("TDF", "onError : " + message);
        trainspotterDialog.showErrorMessage(message);
        trainspotterDialog.stopProgressDialog();
    }

    @Override
    public void onCompletedLoading() {
        trainspotterDialog.stopProgressDialog();
    }

    // Called if we're showing a notification from Firebase messaging, just add the one sighting...
    public void setNotifyFor(TrainParcel trainParcel) {
        this.trainParcel = trainParcel;
        Log.i("TDF", "Setting train from parcel");
        setShowDetailsForTrain(trainParcel.getTrainClass(), trainParcel.getTrainNum());
        Log.i("TDF", "Values : " + currentTrain.getTrain().getNumber());
    }
}
