package uk.co.tezk.trainspotter.model;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import uk.co.tezk.trainspotter.TrainSpotterApplication;
import uk.co.tezk.trainspotter.presenter.ILocationUpdatePresenter;


/**
 * Class to get location from GoogleApi
 */

public class MyLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ILocationUpdatePresenter.IPresenter {

    ILocationUpdatePresenter.IView iView;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    public MyLocation() {
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(TrainSpotterApplication.getApplication())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                //          mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                //         mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                getLocation();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("ML", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("ML", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void bind(ILocationUpdatePresenter.IView view) {
        this.iView = view;
    }

    @Override
    public void unBind() {
        this.iView = null;
    }

    @Override
    public void getLocation() {
        if (mLastLocation != null && iView != null)
            iView.updatePosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
    }

}

