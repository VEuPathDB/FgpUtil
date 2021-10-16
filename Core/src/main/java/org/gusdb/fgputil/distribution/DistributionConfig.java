package org.gusdb.fgputil.distribution;

public class DistributionConfig<T extends Number> {

  private final T _displayRangeMin;
  private final T _displayRangeMax;
  private final T _rangeMin;
  private final T _rangeMax;
  private final T _binWidth;
  private final T _binWidthOverride;

  public DistributionConfig(
      T displayRangeMin, T displayRangeMax,
      T rangeMin, T rangeMax,
      T binWidth, T binWidthOverride) {
    _displayRangeMin = displayRangeMin;
    _displayRangeMax = displayRangeMax;
    _rangeMin = rangeMin;
    _rangeMax = rangeMax;
    _binWidthOverride = binWidthOverride;
    _binWidth = binWidth;
  }

  public T getDisplayRangeMin() {
    return _displayRangeMin;
  }

  public T getDisplayRangeMax() {
    return _displayRangeMax;
  }

  public T getRangeMin() {
    return _rangeMin;
  }

  public T getRangeMax() {
    return _rangeMax;
  }

  public T getBinWidthOverride() {
    return _binWidthOverride;
  }

  public T getBinWidth() {
    return _binWidth;
  }

  public T getDefaultBinWidth() {
    return _binWidthOverride == null ? _binWidth : _binWidthOverride;
  }
}
