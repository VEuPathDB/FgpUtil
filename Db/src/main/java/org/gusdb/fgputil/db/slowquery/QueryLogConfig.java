package org.gusdb.fgputil.db.slowquery;

public interface QueryLogConfig {

  public static final double DEFAULT_BASELINE = 0.1;
  public static final double DEFAULT_SLOW = 5;

  default double getBaseline() { return DEFAULT_BASELINE; }

  default double getSlow() { return DEFAULT_SLOW; }

  /**
   * @param sql sql to decide whether to ignore or not
   */
  default boolean isIgnoredSlow(String sql) { return false; }

  /**
   * @param sql sql to decide whether to ignore or not
   */
  default boolean isIgnoredBaseline(String sql) { return false; }

}
