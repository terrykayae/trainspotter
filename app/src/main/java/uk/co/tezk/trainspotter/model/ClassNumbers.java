package uk.co.tezk.trainspotter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClassNumbers {

@SerializedName("class_number")
@Expose
private List<String> classNumbers = null;

public List<String> getClassNumbers() {
return classNumbers;
}

public void setClassNumbers(List<String> classNumbers) {
this.classNumbers = classNumbers;
}

}