package org.gusdb.fgputil.distribution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subsetSize",
    "subsetMin",
    "subsetMax",
    "subsetMean",
    "numVarValues",
    "numDistinctValues",
    "numDistinctEntityRecords",
    "numMissingCases"
})
public class HistogramStats {

  @JsonProperty("subsetSize")
  private Long subsetSize;

  @JsonProperty("subsetMin")
  private Object subsetMin;

  @JsonProperty("subsetMax")
  private Object subsetMax;

  @JsonProperty("subsetMean")
  private Object subsetMean;

  @JsonProperty("numVarValues")
  private Long numVarValues;

  @JsonProperty("numDistinctValues")
  private Long numDistinctValues;

  @JsonProperty("numDistinctEntityRecords")
  private Long numDistinctEntityRecords;

  @JsonProperty("numMissingCases")
  private Long numMissingCases;

  @JsonProperty("subsetSize")
  public Long getSubsetSize() {
    return this.subsetSize;
  }

  @JsonProperty("subsetSize")
  public void setSubsetSize(Long subsetSize) {
    this.subsetSize = subsetSize;
  }

  @JsonProperty("subsetMin")
  public Object getSubsetMin() {
    return this.subsetMin;
  }

  @JsonProperty("subsetMin")
  public void setSubsetMin(Object subsetMin) {
    this.subsetMin = subsetMin;
  }

  @JsonProperty("subsetMax")
  public Object getSubsetMax() {
    return this.subsetMax;
  }

  @JsonProperty("subsetMax")
  public void setSubsetMax(Object subsetMax) {
    this.subsetMax = subsetMax;
  }

  @JsonProperty("subsetMean")
  public Object getSubsetMean() {
    return this.subsetMean;
  }

  @JsonProperty("subsetMean")
  public void setSubsetMean(Object subsetMean) {
    this.subsetMean = subsetMean;
  }

  @JsonProperty("numVarValues")
  public Long getNumVarValues() {
    return this.numVarValues;
  }

  @JsonProperty("numVarValues")
  public void setNumVarValues(Long numVarValues) {
    this.numVarValues = numVarValues;
  }

  @JsonProperty("numDistinctValues")
  public Long getNumDistinctValues() {
    return this.numDistinctValues;
  }

  @JsonProperty("numDistinctValues")
  public void setNumDistinctValues(Long numDistinctValues) {
    this.numDistinctValues = numDistinctValues;
  }

  @JsonProperty("numDistinctEntityRecords")
  public Long getNumDistinctEntityRecords() {
    return this.numDistinctEntityRecords;
  }

  @JsonProperty("numDistinctEntityRecords")
  public void setNumDistinctEntityRecords(Long numDistinctEntityRecords) {
    this.numDistinctEntityRecords = numDistinctEntityRecords;
  }

  @JsonProperty("numMissingCases")
  public Long getNumMissingCases() {
    return this.numMissingCases;
  }

  @JsonProperty("numMissingCases")
  public void setNumMissingCases(Long numMissingCases) {
    this.numMissingCases = numMissingCases;
  }
}

