package uk.co.tezk.trainspotter.model;

import io.realm.RealmObject;

/**
 * Model realm object to store details of images
 */

public class ImageDetails extends RealmObject {
    private String imageUrl;
    private String date;
    private String time;
    private String classNum;
    private String trainNum;
    private String locationName;
    private Boolean takenByUs;  // images taken by us will be locally stored? If not, they're remote

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClassNum() {
        return classNum;
    }

    public void setClassNum(String classNum) {
        this.classNum = classNum;
    }

    public String getTrainNum() {
        return trainNum;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Boolean getTakenByUs() {
        return takenByUs;
    }

    public void setTakenByUs(Boolean takenByUs) {
        this.takenByUs = takenByUs;
    }
}
