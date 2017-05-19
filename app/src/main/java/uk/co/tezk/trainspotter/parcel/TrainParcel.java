package uk.co.tezk.trainspotter.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tezk on 19/05/17.
 */

public class TrainParcel implements Parcelable {
    float lat;
    float lon;
    String trainClass;
    String trainNum;

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
}
