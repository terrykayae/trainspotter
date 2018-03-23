package uk.co.tezk.trainspotter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class TrainListItem extends RealmObject {
    @SerializedName("trainClass")
    @Expose
    public String trainClass;
    @SerializedName("number")
    @Expose
    public String number;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("livery")
    @Expose
    private String livery;
    @SerializedName("pool")
    @Expose
    private String pool;
    @SerializedName("operator")
    @Expose
    private String operator;
    @SerializedName("depot")
    @Expose
    private String depot;

    public TrainListItem() {
    }

    public TrainListItem(String trainClass, String number, String name, String livery, String pool, String operator, String depot) {
        this.trainClass = trainClass == null ? "" : trainClass;
        this.number = number == null ? "" : number;
        this.name = name == null ? "" : name;
        this.livery = livery == null ? "" : livery;
        this.pool = pool == null ? "" : pool;
        this.operator = operator == null ? "" : operator;
        this.depot = depot == null ? "" : depot;
    }

    public String getClass_() {
        return trainClass;
    }

    public void setClass_(String trainClass) {
        this.trainClass = trainClass;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLivery() {
        return livery;
    }

    public void setLivery(String livery) {
        this.livery = livery;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDepot() {
        return depot;
    }

    public void setDepot(String depot) {
        this.depot = depot;
    }

}