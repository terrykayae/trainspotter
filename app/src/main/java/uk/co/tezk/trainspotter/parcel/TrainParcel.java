package uk.co.tezk.trainspotter.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by tezk on 19/05/17.
 */

public class TrainParcel implements Parcelable {
    float lat;
    float lon;
    String trainClass;
    String trainNum;

    public TrainParcel(float lat, float lon, String trainClass, String trainNum) {
        this.lat = lat;
        this.lon = lon;
        this.trainClass = trainClass;
        this.trainNum = trainNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(lat);
        dest.writeFloat(lon);
        dest.writeString(trainClass);
        dest.writeString(trainNum);
    }

    private TrainParcel(Parcel source) {
        lat = source.readFloat();
        lon = source.readFloat();
        trainClass = source.readString();
        trainNum = source.readString();
    }

    public static Parcelable.Creator<TrainParcel> CREATOR = new Parcelable.Creator<TrainParcel>() {
        @Override
        public TrainParcel createFromParcel(Parcel source) {
            return new TrainParcel(source);
        }

        @Override
        public TrainParcel[] newArray(int size) {
            return new TrainParcel[size];
        }
    };

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

    public String getTrainClass() {
        return trainClass;
    }

    public void setTrainClass(String trainClass) {
        this.trainClass = trainClass;
    }

    public String getTrainNum() {
        Log.i("TrainParcel", "train num = "+trainNum);
        return trainNum;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }
}
