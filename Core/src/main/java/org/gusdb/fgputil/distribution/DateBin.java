package org.gusdb.fgputil.distribution;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.gusdb.fgputil.FormatUtil;

public class DateBin implements AbstractBinDistribution.Bin<LocalDateTime> {

  public final LocalDateTime _start;
  public final LocalDateTime _end;
  private final ChronoUnit _binUnits;
  private final int _binSize;

  private long _binCount = 0;

  public DateBin(LocalDateTime start, LocalDateTime end, ChronoUnit binUnits, int binSize) {
    _start = start;
    _end = end;
    _binUnits = binUnits;
    _binSize = binSize;
  }

  @Override
  public boolean startsAfter(LocalDateTime value) {
    return _start.isAfter(value);
  }

  @Override
  public boolean accept(LocalDateTime value, Long count) {
    // start inclusive, end exclusive
    if (!value.isBefore(_start) && value.isBefore(_end)) {
      _binCount += count;
      return true;
    }
    return false;
  }

  @Override
  public HistogramBin toHistogramBin() {
    HistogramBin bin = new HistogramBin();
    bin.setBinStart(FormatUtil.formatDateTime(_start).substring(0, 10));
    bin.setBinEnd(FormatUtil.formatDateTime(_end).substring(0, 10));
    bin.setBinLabel(getBinLabel(bin));
    bin.setValue(_binCount);
    return bin;
  }

  private String getBinLabel(HistogramBin bin) {
    switch (_binUnits) {
      case YEARS:
        return _binSize == 1 ? getYearDisplay(_start) :
            getYearDisplay(_start) + " - " + getYearDisplay(_end);
      case MONTHS:
        return _binSize == 1 ? getMonthDisplay(_start) :
            getMonthDisplay(_start) + " - " + getMonthDisplay(_end);
      case DAYS:
        return _binSize == 1 ? bin.getBinStart() : getDayRangeDisplay(bin);
      case WEEKS:
      default:
        return getDayRangeDisplay(bin);
    }
  }

  private static String getDayRangeDisplay(HistogramBin bin) {
    return "[" + bin.getBinStart() + "," + bin.getBinEnd() + ")";
  }

  private static String getYearDisplay(LocalDateTime date) {
    return String.valueOf(date.getYear());
  }

  private static String getMonthDisplay(LocalDateTime date) {
    return date.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US) + " " + getYearDisplay(date);
  }
}
