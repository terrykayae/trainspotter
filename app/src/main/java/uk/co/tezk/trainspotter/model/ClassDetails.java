package uk.co.tezk.trainspotter.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Used to store the class details locally - record for each number with an entry that shows total sightings made for class
 */

public class ClassDetails extends RealmObject {
    @PrimaryKey
    private String classId;
    private Integer sightingsRecorded;
    private String classCategory; // Currently not used, but might be useful in future to group the classes
    private Integer totalTrains;

    public Integer getTotalTrains() {
        return totalTrains;
    }

    public void setTotalTrains(Integer totalTrains) {
        this.totalTrains = totalTrains;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Integer getSightingsRecorded() {
        return sightingsRecorded;
    }

    public void setSightingsRecorded(Integer sightingsRecorded) {
        this.sightingsRecorded = sightingsRecorded;
    }

    public String getClassCategory() {
        return classCategory;
    }

    public void setClassCategory(String classCategory) {
        this.classCategory = classCategory;
    }
}
