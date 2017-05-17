package uk.co.tezk.trainspotter.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tezk on 17/05/17.
 */

public class MapViewParcelable implements Parcelable {
    double lat;
    double lon;
    float zoomLevel;
    float bearing;
    float tilt;

    public MapViewParcelable() {}

    public MapViewParcelable(double lat, double lon, float zoomLevel, float bearing, float tilt) {
        this.lat = lat;
        this.lon = lon;
        this.zoomLevel = zoomLevel;
        this.bearing = bearing;
        this.tilt = tilt;
    }

    @Override
    public int describeContents() {
        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeFloat(zoomLevel);
        dest.writeFloat(bearing);
        dest.writeFloat(tilt);
    }

    private MapViewParcelable(Parcel inParcel) {
        lat = inParcel.readDouble();
        lon = inParcel.readDouble();
        zoomLevel = inParcel.readFloat();
        bearing = inParcel.readFloat();
        tilt = inParcel.readFloat();
    }

    public static Parcelable.Creator <MapViewParcelable> CREATOR
            = new Parcelable.Creator<MapViewParcelable>() {
        @Override
        public MapViewParcelable createFromParcel(Parcel source) {
            return new MapViewParcelable(source);
        }

        @Override
        public MapViewParcelable[] newArray(int size) {
            return new MapViewParcelable[size];
        }
    };

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getTilt() {
        return tilt;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }
}
