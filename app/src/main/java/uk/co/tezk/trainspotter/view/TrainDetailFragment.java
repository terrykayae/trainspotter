package uk.co.tezk.trainspotter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.tezk.trainspotter.MainActivity;
import uk.co.tezk.trainspotter.R;
import uk.co.tezk.trainspotter.Utilitity;
import uk.co.tezk.trainspotter.model.Constant;
import uk.co.tezk.trainspotter.model.SightingDetails;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;
import uk.co.tezk.trainspotter.presenter.ITrainDetailPresenter;
import uk.co.tezk.trainspotter.presenter.TrainDetailPresenterImpl;

import static uk.co.tezk.trainspotter.model.Constant.MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS;

/**
 * A simple {@link Fragment} subclass.
 */

public class TrainDetailFragment extends Fragment implements
        OnMapReadyCallback,
        ITrainDetailPresenter.IView {
    // Textview fields
    @BindView(R.id.tvClass)TextView tvClass;
    @BindView(R.id.tvTrainNumber)TextView tvTrainNumber;
    @BindView(R.id.tvTrainName)TextView tvTrainName;
    @BindView(R.id.tvTrainLivery)TextView tvTrainLivery;
    @BindView(R.id.tvTrainOperator)TextView tvTrainOperator;
    @BindView(R.id.tvTrainDepot)TextView tvTrainDepot;
    @BindView(R.id.tvTrainLastSpotted)TextView tvTrainLastSpotted;
    @BindView(R.id.tvTrainWhere)TextView tvTrainWhere;
    // Holder for the map
   // @BindView(R.id.mapHolder) FrameLayout mMapHolder;

    Context context;
    SupportMapFragment mSupportMapFragment;
    GoogleMap mGoogleMap;
    // Include getter for GoogleMap so mainActivity can enable location
    public GoogleMap getGoogleMap() { return mGoogleMap; }

    TrainDetail currentTrain;
    // Include getter so Activity can see what we're showing
    public TrainDetail getCurrentTrain() { return currentTrain; }

    ITrainDetailPresenter.IPresenter presenter;


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTrainDetailFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAddSightingForTrain(String classNum, String trainNum) ;
    }
    private OnTrainDetailFragmentInteractionListener mListener;

    public TrainDetailFragment() {
        // Required empty public constructor
    }

    public void setShowDetailsForTrain(String classNumber, String trainNumber) {
        // if train == 1, get first for class
        if (currentTrain == null) {
            currentTrain = new TrainDetail();
            TrainListItem trainListItem = new TrainListItem();
            currentTrain.setTrain(trainListItem);
        }
        currentTrain.getTrain().setClass_(classNumber);
        currentTrain.getTrain().setNumber(trainNumber);

        Log.i("TDF", "setting currentTrain = "+currentTrain);
        Log.i("TDF", "class "+currentTrain.getTrain().getClass_());
        Log.i("TDF", "id "+currentTrain.getTrain().getNumber());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new TrainDetailPresenterImpl();
        if (savedInstanceState!=null) {
            String classNum = savedInstanceState.getString(Constant.CLASS_NUM_KEY);
            String trainNum = savedInstanceState.getString(Constant.TRAIN_NUM_KEY);
            if (classNum!=null && trainNum!=null) {
                setShowDetailsForTrain(classNum, trainNum);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_train_detail, container, false);
        // Butter knife bindings
        ButterKnife.bind(this, view);

        presenter.bind(this);
        return view;
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("TDF", "currentTrain = "+currentTrain);
        Log.i("TDF", "class "+currentTrain.getTrain().getClass_());
        Log.i("TDF", "id "+currentTrain.getTrain().getNumber());

        if (currentTrain!=null)
            presenter.retrieveData(currentTrain.getTrain().getClass_(), currentTrain.getTrain().getNumber());
        // Load the map
        FragmentManager fragmentManager = ((MainActivity)context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mSupportMapFragment = SupportMapFragment.newInstance();
        transaction.replace(R.id.mapHolder, mSupportMapFragment);
        mSupportMapFragment.getMapAsync(this);
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentTrain!=null) {
            outState.putString(Constant.CLASS_NUM_KEY, currentTrain.getTrain().getClass_());
            outState.putString(Constant.TRAIN_NUM_KEY, currentTrain.getTrain().getNumber());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unbind();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // Enable the setMyLocation, if we've got permission. If not this will askk
        try {
            if (Utilitity.checkLocationPermissions(context, MY_PERMISSIONS_REQUEST_LOCATION_FROM_DETAILS)) {
                googleMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Display flags for any sightings!
        addSightingsToMap();
    }

    public void addSightingsToMap() {
        if (currentTrain.getSightings() != null && currentTrain.getSightings().size() > 0) {
            BitmapDescriptor flagIcon = BitmapDescriptorFactory.fromResource(R.drawable.filled_flag_xxl)
            for (SightingDetails each : currentTrain.getSightings())  {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(each.getLat(), each.getLon()))
                        .title(each.getDate())
                        .icon(flagIcon)
                        .anchor(0.2f, 1f);

                Marker marker = mGoogleMap.addMarker(markerOptions);
            }
        }
    }

    // Called by our presenter
    @Override
    public void showTrainDetails(TrainDetail trainDetail) {
        Log.i("TDF", "showTrainDetails "+trainDetail);
        this.currentTrain = trainDetail;
        tvClass.setText(trainDetail.getTrain().getClass_());
        tvTrainNumber.setText(trainDetail.getTrain().getNumber());
        tvTrainName.setText(trainDetail.getTrain().getName());
        tvTrainLivery.setText(trainDetail.getTrain().getLivery());
        tvTrainOperator.setText(trainDetail.getTrain().getOperator());
        tvTrainDepot.setText(trainDetail.getTrain().getDepot());
        if (trainDetail.getSightings() == null || trainDetail.getSightings().size()==0) {
            tvTrainLastSpotted.setText(getText(R.string.none_reported));
        } else {
            SightingDetails latestSighting = trainDetail.getSightings().get(0);
            for (SightingDetails each : trainDetail.getSightings()) {
                if (each.getDate().compareTo(latestSighting.getDate())>0) {
                    latestSighting = each;
                }
            }
            tvTrainLastSpotted.setText(latestSighting.getDate());
            if (latestSighting.getLocationName()!=null && latestSighting.getLocationName().length()>0) {
                tvTrainWhere.setText(latestSighting.getLocationName());
            } else {
                tvTrainWhere.setText(latestSighting.getLat()+", "+latestSighting.getLon());
            }
        }
    }

    @Override
    public void onStartLoading() {

    }

    @Override
    public void onErrorLoading(String message) {
        Log.i("TDF", "Error : "+message);
    }

    @Override
    public void onCompletedLoading() {

    }
}
