package uk.co.tezk.trainspotter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Model object to manipulate sightings - used to store locally made sightings and sightings others have made that
 * are retrieved from the API but not stored locally
 */

public class SightingDetails extends RealmObject {
    // These fields are used only for the local Realm sightings that this user makes, the API doesn't make use of them
    private String trainId;
    private String trainClass;
    // if time is null for any sighting, this marks it as a sighting pulled from the API
    private String time;
    private String locationName;
    // These fields are also stored in Realm, but are filled by calls to the API
    @SerializedName("lat")
    @Expose
    private float lat;
    @SerializedName("lon")
    @Expose
    private float lon;
    @SerializedName("date")
    @Expose
    private String date;

    public SightingDetails() {}

    public SightingDetails(String date, float lat, float lon) {
        this.date = date;
        this.lat = lat;
        this.lon = lon;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTrainClass() {
        return trainClass;
    }

    public void setTrainClass(String trainClass) {
        this.trainClass = trainClass;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
