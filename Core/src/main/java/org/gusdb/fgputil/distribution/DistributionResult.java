package org.gusdb.fgputil.distribution;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionResult {

  private final List<HistogramBin> _histogram;
  private final HistogramStats _stats;

  public DistributionResult(List<HistogramBin> histogram, HistogramStats stats) {
    _histogram = histogram;
    _stats = stats;
  }

  @JsonProperty("histogram")
  public List<HistogramBin> getHistogramData() {
    return _histogram;
  }

  public HistogramStats getStatistics() {
    return _stats;
  }

}
