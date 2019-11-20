package org.gusdb.fgputil;

/**
 * Enumeration of sort directions. Also provides translation between legacy
 * sort direction indicators (booleans) and these values.
 *
 * @author rdoherty
 */
public enum SortDirection {

  ASC  (true),
  DESC (false);

  private boolean _boolValue;

  SortDirection(boolean boolValue) {
    _boolValue = boolValue;
  }

  // deprecated pending refactoring of WDK sort ordering code
  public boolean isAscending() {
    return _boolValue;
  }

  public static boolean isValidDirection(String str) {
    try {
      valueOf(str);
      return true;
    }
    catch (IllegalArgumentException | NullPointerException e) {
      return false;
    }
  }

  // deprecated pending refactoring of WDK sort ordering code
  public static SortDirection getFromIsAscending(Boolean isAscending) {
    for (SortDirection d : values()) {
      if (d._boolValue == isAscending) return d;
    }
    throw new IllegalArgumentException("No direction has bool value " + isAscending);
  }
}
