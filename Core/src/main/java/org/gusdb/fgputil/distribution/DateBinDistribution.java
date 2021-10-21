package org.gusdb.fgputil.distribution;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.FormatUtil;

public class DateBinDistribution extends AbstractBinDistribution<LocalDateTime, DateBin> {

  private static final List<ChronoUnit> ALLOWED_UNITS = List.of(
      ChronoUnit.DAYS,
      ChronoUnit.WEEKS,
      ChronoUnit.MONTHS,
      ChronoUnit.YEARS
  );

  public interface DateBinSpec {
    ChronoUnit getBinUnits();
    int getBinSize();
    Object getDisplayRangeMin();
    Object getDisplayRangeMax();
  }

  private final LocalDateTime _displayMin;
  private final LocalDateTime _displayMax;
  private final int _binSize;
  private final ChronoUnit _binUnits;

  public DateBinDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec, DateBinSpec binSpec) {
    super(streamProvider, valueSpec);

    // process the bin spec to configure this distribution
    _binUnits = validateBinUnits(binSpec.getBinUnits());
    _binSize = validateBinSize(binSpec.getBinSize());
    _displayMin = getDisplayMin(binSpec.getDisplayRangeMin());
    _displayMax = getDisplayMax(binSpec.getDisplayRangeMax());
  }

  private ChronoUnit validateBinUnits(ChronoUnit binUnits) {
    if (!ALLOWED_UNITS.contains(binUnits)) {
      throw new IllegalArgumentException("Cannot create date distribution with units: " + binUnits);
    }
    return binUnits;
  }

  private int validateBinSize(int rawBinSize) {
    if (rawBinSize <= 0) {
      throw new IllegalArgumentException("binWidth must be a positive integer for date variable distributions");
    }
    return rawBinSize;
  }

  private LocalDateTime getDisplayMin(Object rawMinValue) {
    LocalDateTime rawDateTime = getTypedObject("displayRangeMin", rawMinValue, ValueSource.CONFIG);
    switch(_binUnits) {
      case MONTHS:
        // truncate to the beginning of the month
        return LocalDateTime.of(rawDateTime.getYear(), rawDateTime.getMonth(), 1, 0, 0);
      case YEARS:
        // truncate to the beginning of the year
        return LocalDateTime.of(rawDateTime.getYear(), Month.JANUARY, 1, 0, 0);
      default:
        // for days and weeks, truncate to the beginning of the day
        return rawDateTime.truncatedTo(ChronoUnit.DAYS);
    }
  }

  private LocalDateTime getDisplayMax(Object rawMaxValue) {
    LocalDateTime rawDateTime = getTypedObject("displayMax", rawMaxValue, ValueSource.CONFIG);
    // truncate to the day (floor), then add 1 day and subtract 1 second
    //    this way all values on that day fall before the max, but none after
    return rawDateTime
        .truncatedTo(ChronoUnit.DAYS)
        .plus(1, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.SECONDS);
  }

  @Override
  protected LocalDateTime getTypedObject(String objectName, Object value, ValueSource source) {
    if (value instanceof String) {
      return FormatUtil.parseDateTime((String)value);
    }
    switch(source) {
      case CONFIG: throw new IllegalArgumentException(objectName + " must be a date string value.");
      case DB: throw new RuntimeException("Converted value in column " + objectName + " is not a valid date string.");
      default: throw new IllegalStateException("Unsupported source: " + source);
    }
  }

  @Override
  protected StatsCollector<LocalDateTime> getStatsCollector() {
    return new StatsCollector<>() {

      private long _sumOfValues = 0;

      @Override
      public void accept(LocalDateTime value, Long count) {
        super.accept(value, count);
        _sumOfValues += (count * value.toEpochSecond(ZoneOffset.UTC));
      }

      @Override
      public HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount) {
        HistogramStats stats = super.toHistogramStats(subsetEntityCount, missingCasesCount);
        // check if data present to avoid when filters leave no data for the current variable
        if (isDataPresent()) {
          // override the LocalDateTime objects set by the parent class and assign strings
          stats.setSubsetMin(FormatUtil.formatDateTime(_subsetMin));
          stats.setSubsetMax(FormatUtil.formatDateTime(_subsetMax));
          stats.setSubsetMean(FormatUtil.formatDateTime(
              LocalDateTime.ofEpochSecond(_sumOfValues / stats.getNumVarValues(), 0, ZoneOffset.UTC)));
        }
        return stats;
      }
    };
  }

  @Override
  protected DateBin getFirstBin() {
    return getNextBin(new DateBin(null, _displayMin, _binUnits, _binSize)).orElseThrow();
  }

  @Override
  protected Optional<DateBin> getNextBin(DateBin currentBin) {
    return _displayMax.isBefore(currentBin._end) ? Optional.empty() :
        Optional.of(new DateBin(currentBin._end, currentBin._end.plus(_binSize, _binUnits), _binUnits, _binSize));
  }

}
