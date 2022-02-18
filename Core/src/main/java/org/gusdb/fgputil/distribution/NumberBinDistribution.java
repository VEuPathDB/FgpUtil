package org.gusdb.fgputil.distribution;

import java.util.Optional;

public abstract class NumberBinDistribution<T extends Number & Comparable<T>> extends AbstractBinDistribution<T, NumberBin<T>>{

  public interface NumberBinSpec {
    Object getDisplayRangeMin();
    Object getDisplayRangeMax();
    Object getBinSize();
  }

  protected final T _displayRangeMin;
  protected final T _displayRangeMax;
  protected final T _binWidth;

  // utility for less than; I always have to check what compareTo() result means
  public static <S extends Comparable<S>> boolean isLessThan(S leftOperand, S rightOperand) {
    return leftOperand.compareTo(rightOperand) < 0;
  }

  // return the sum of the two numbers
  protected abstract T sum(T a, T b);

  // validate bin width
  protected abstract void validateBinWidth(T binWidth) throws IllegalArgumentException;

  public NumberBinDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec, NumberBinSpec binSpec) {
    super(streamProvider, valueSpec);
    _displayRangeMin = getRangeMin(binSpec);
    _displayRangeMax = getRangeMax(binSpec);
    _binWidth = getBinWidth(binSpec);
  }

  private T getRangeMin(NumberBinSpec binSpec) {
    return getTypedObject("displayRangeMin", binSpec.getDisplayRangeMin(), ValueSource.CONFIG);
  }

  private T getRangeMax(NumberBinSpec binSpec) {
    return getTypedObject("displayRangeMax", binSpec.getDisplayRangeMax(), ValueSource.CONFIG);
  }

  private T getBinWidth(NumberBinSpec binSpec) {
    T binWidth = getTypedObject("binWidth", binSpec.getBinSize(), ValueSource.CONFIG);
    validateBinWidth(binWidth);
    return binWidth;
  }

  @Override
  protected NumberBin<T> getFirstBin() {
    return getNextBin(new NumberBin<>(null, _displayRangeMin)).orElseThrow();
  }

  @Override
  protected Optional<NumberBin<T>> getNextBin(NumberBin<T> currentBin) {
    return isLessThan(_displayRangeMax, currentBin._end) ? Optional.empty() :
        Optional.of(new NumberBin<T>(currentBin._end, sum(currentBin._end, _binWidth)));
  }

}
