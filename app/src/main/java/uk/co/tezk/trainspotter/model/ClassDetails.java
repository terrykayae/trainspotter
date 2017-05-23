package uk.co.tezk.trainspotter.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Used to store the class details locally - record for each number with an entry that shows total sightings made for class
 */

public class ClassDetails extends RealmObject implements Comparable {
    @PrimaryKey
    private String classId;
    private Integer sightingsRecorded;

    @Override
    public int compareTo(@NonNull Object o) {
        try {
            return Integer.parseInt(classId) - Integer.parseInt(((ClassDetails) o).getClassId());
        } catch ( Exception e ) {
            return 0;
        }
    }

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

    @Override
    public int hashCode() {
        return classId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ClassDetails))
            return false;
        ClassDetails classDetails = (ClassDetails)obj;
        return classId.equals(classDetails.classId);
    }
}
