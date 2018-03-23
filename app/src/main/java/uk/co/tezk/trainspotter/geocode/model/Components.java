package uk.co.tezk.trainspotter.geocode.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Components {

@SerializedName("_type")
@Expose
private String type;
@SerializedName("city")
@Expose
private String city;
@SerializedName("country")
@Expose
private String country;
@SerializedName("country_code")
@Expose
private String countryCode;
@SerializedName("house_number")
@Expose
private String houseNumber;
@SerializedName("neighbourhood")
@Expose
private String neighbourhood;
@SerializedName("postcode")
@Expose
private String postcode;
@SerializedName("road")
@Expose
private String road;
@SerializedName("state")
@Expose
private String state;
@SerializedName("state_district")
@Expose
private String stateDistrict;
@SerializedName("suburb")
@Expose
private String suburb;

public String getType() {
return type;
}

public void setType(String type) {
this.type = type;
}

public String getCity() {
return city;
}

public void setCity(String city) {
this.city = city;
}

public String getCountry() {
return country;
}

public void setCountry(String country) {
this.country = country;
}

public String getCountryCode() {
return countryCode;
}

public void setCountryCode(String countryCode) {
this.countryCode = countryCode;
}

public String getHouseNumber() {
return houseNumber;
}

public void setHouseNumber(String houseNumber) {
this.houseNumber = houseNumber;
}

public String getNeighbourhood() {
return neighbourhood;
}

public void setNeighbourhood(String neighbourhood) {
this.neighbourhood = neighbourhood;
}

public String getPostcode() {
return postcode;
}

public void setPostcode(String postcode) {
this.postcode = postcode;
}

public String getRoad() {
return road;
}

public void setRoad(String road) {
this.road = road;
}

public String getState() {
return state;
}

public void setState(String state) {
this.state = state;
}

public String getStateDistrict() {
return stateDistrict;
}

public void setStateDistrict(String stateDistrict) {
this.stateDistrict = stateDistrict;
}

public String getSuburb() {
return suburb;
}

public void setSuburb(String suburb) {
this.suburb = suburb;
}

}