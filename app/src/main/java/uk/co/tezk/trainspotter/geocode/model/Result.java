package uk.co.tezk.trainspotter.geocode.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

@SerializedName("components")
@Expose
private Components components;
@SerializedName("confidence")
@Expose
private Integer confidence;
@SerializedName("formatted")
@Expose
private String formatted;

public Components getComponents() {
return components;
}

public void setComponents(Components components) {
this.components = components;
}

public Integer getConfidence() {
return confidence;
}

public void setConfidence(Integer confidence) {
this.confidence = confidence;
}

public String getFormatted() {
return formatted;
}

public void setFormatted(String formatted) {
this.formatted = formatted;
}

}