package uk.co.tezk.trainspotter.geocode.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Geocoder {

@SerializedName("documentation")
@Expose
private String documentation;

@SerializedName("results")
@Expose
private List<Result> results = null;
@SerializedName("status")
@Expose
private Status status;

@SerializedName("thanks")
@Expose
private String thanks;

@SerializedName("total_results")
@Expose
private Integer totalResults;

public String getDocumentation() {
return documentation;
}

public void setDocumentation(String documentation) {
this.documentation = documentation;
}

public List<Result> getResults() {
return results;
}

public void setResults(List<Result> results) {
this.results = results;
}

public Status getStatus() {
return status;
}

public void setStatus(Status status) {
this.status = status;
}

public String getThanks() {
return thanks;
}

public void setThanks(String thanks) {
this.thanks = thanks;
}

public Integer getTotalResults() {
return totalResults;
}

public void setTotalResults(Integer totalResults) {
this.totalResults = totalResults;
}

}