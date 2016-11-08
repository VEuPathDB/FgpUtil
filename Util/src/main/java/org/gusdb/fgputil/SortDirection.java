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

  private SortDirection(boolean boolValue) {
    _boolValue = boolValue;
  }

  public boolean getBoolValue() {
    return _boolValue;
  }

  public static boolean isDirection(String str) {
    try {
      valueOf(str);
      return true;
    }
    catch (IllegalArgumentException | NullPointerException e) {
      return false;
    }
  }

  public static SortDirection fromBoolean(Boolean value) {
    for (SortDirection d : values()) {
      if (d._boolValue == value) return d;
    }
    throw new IllegalArgumentException("No direction has bool value " + value);
  }
}
