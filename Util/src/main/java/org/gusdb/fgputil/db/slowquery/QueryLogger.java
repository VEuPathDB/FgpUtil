package org.gusdb.fgputil.db.slowquery;

import static org.gusdb.fgputil.FormatUtil.getInnerClassLog4jName;

import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner;

public class QueryLogger {

  private static final Logger LOG = Logger.getLogger(QueryLogger.class);

  private static QueryLoggerInstance _instance;

  // static class cannot be instantiated
  private QueryLogger() {}

  public static synchronized void initialize(QueryLogConfig config) {
    if (_instance != null) {
      LOG.warn("Multiple calls to initialize().  Ignoring...");
    }
    else {
      LOG.info("Initializing QueryLogger (slow and example query logs)");
      ExampleQueryLog.getLogger().debug("Initializing example query log");
      SlowQueryLog.getLogger().debug("Initializing slow query log");
      _instance = new QueryLoggerInstance(config);
      SQLRunner.setSqlLogger(_instance);
    }
  }

  public static synchronized void setInactive() {
    initialize(new QueryLogConfig() {
      @Override public double getBaseline() { return 5; }
      @Override public double getSlow() { return 5; }
      @Override public boolean isIgnoredSlow(String sql) { return true; }
      @Override public boolean isIgnoredBaseline(String sql) { return true; }
    });
  }

  /** 
   * Call this version to track and log query time if you do not have a resultSet, eg, for an update or insert.  Call it after the execute.
   * 
   * @param sql SQL of statement being logged
   * @param name name of operation
   * @param startTime start time in ms of operation to be compared to "now" (the end time)
   */
  public static void logEndStatementExecution(String sql, String name, long startTime) {
    checkInstance();
    _instance.logQueryTime(sql, name, startTime, -1, System.currentTimeMillis(), false);
  }

  /** 
   * Call this version to track and log query time if you are using a ResultSet.  Call it after the execute but before iterating through the resultSet.
   * When done with the result set use one of SqlUtil's close methods that take a ResultSet argument to close it.
   * 
   * @param sql SQL statement to log
   * @param name name of this operation
   * @param startTime start time (ms) of this operation (to be compared to end time later)
   * @param resultSet result set being processed
   */
  public static void logStartResultsProcessing(String sql, String name, long startTime, ResultSet resultSet) {
    checkInstance();
    _instance.logStartResultsProcessing(sql, name, startTime, resultSet);
  }

  public static void logEndResultsProcessing(ResultSet resultSet) {
    checkInstance();
    _instance.logEndResultsProcessing(resultSet);
  }

  public static void logOrphanedResultSets() {
    checkInstance();
    _instance.logOrphanedResultSets();
  }

  private static void checkInstance() {
    if (_instance == null) {
      throw new IllegalStateException("The initialize() method must be called on this class before any other method is called.");
    }
  }

  /**
   * Contains timing information for slow queries
   */
  static final class SlowQueryLog {
    private static final Logger _logger = Logger.getLogger(getInnerClassLog4jName(SlowQueryLog.class));
    private SlowQueryLog() {}
    public static Logger getLogger() { return _logger; }
  }

  /**
   * Allows logging of example queries to a separate logger (one line per query)
   */
  static final class ExampleQueryLog {
    private static final Logger _logger = Logger.getLogger(getInnerClassLog4jName(ExampleQueryLog.class));
    private ExampleQueryLog() {}
    public static Logger getLogger() { return _logger; }
  }
}
