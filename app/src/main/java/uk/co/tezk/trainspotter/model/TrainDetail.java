package uk.co.tezk.trainspotter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by tezk on 10/05/17.
 */

public class TrainDetail {
    @SerializedName("train")
    @Expose
    private TrainListItem train;
    @SerializedName("sightings")
    @Expose
    private List<SightingDetails> sightings;
    @SerializedName("images")
    @Expose
    private List<String> images;

    public TrainListItem getTrain() {
        return train;
    }

    public void setTrain(TrainListItem train) {
        this.train = train;
    }

    public List<SightingDetails> getSightings() {
        return sightings;
    }

    public void setSightings(List<SightingDetails> sightings) {
        this.sightings = sightings;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
