package org.gusdb.fgputil.db.slowquery;

import static org.gusdb.fgputil.FormatUtil.getInnerClassLog4jName;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.slowquery.SqlTimer.SqlTimerEvents;

public class QueryLogger {

  private static final Logger LOG = Logger.getLogger(QueryLogger.class);

  /*****************************************************************************
   *
   * QueryLogger is a singleton that should be initialized exactly once on startup
   *
   ****************************************************************************/

  private static QueryLogger _instance;

  public static synchronized void initialize(QueryLogConfig config) {
    if (_instance != null) {
      LOG.warn("Multiple calls to initialize().  Ignoring...");
    }
    else {
      LOG.info("Initializing QueryLogger (slow and example query logs)");
      ExampleQueryLog.getLogger().debug("Initializing example query log");
      SlowQueryLog.getLogger().debug("Initializing slow query log");
      _instance = new QueryLogger(config);
    }
  }

  private static void performIfPresent(Consumer<QueryLogger> action) {
    Optional.ofNullable(_instance).ifPresent(action);
  }

  /*****************************************************************************
   *
   * Instance fields
   *
   ****************************************************************************/

  private final QueryLogConfig _config;
  private final Set<String> _queryNames = new HashSet<>();
  private final Map<ResultSet, QueryLogInfo> _queryLogInfos =
      Collections.synchronizedMap(new HashMap<ResultSet, QueryLogInfo>());

  private QueryLogger(QueryLogConfig config) {
    _config = config;
  }

  /** 
   * Track and log query time if you do not have a resultSet, eg, for an update or insert.  Call it after the execute.
   * 
   * @param sql SQL of statement being logged
   * @param name name of operation
   * @param startTime start time in ms of operation to be compared to "now" (the end time)
   */
  public static void logEndStatementExecution(String sql, String name, long startTime) {
    performIfPresent(ql ->
      ql.logQueryTime(sql, name, startTime, -1, System.currentTimeMillis(), false)
    );
  }

  /** 
   * Track and log query time if you are using a ResultSet.  Call it after the execute but before iterating through the resultSet.
   * When done with the result set use one of SqlUtil's close methods that take a ResultSet argument to close it.
   * 
   * @param sql SQL statement to log
   * @param name name of this operation
   * @param startTime start time (ms) of this operation (to be compared to end time later)
   * @param resultSet result set being processed
   */
  public static void logStartResultsProcessing(String sql, String name,
      long startTime, ResultSet resultSet) {
    performIfPresent(ql ->
      ql._queryLogInfos.put(resultSet, new QueryLogInfo(sql, name, startTime, System.currentTimeMillis()))
    );
  }

  /**
   * Log end of ResultSet processing
   * 
   * @param resultSet
   */
  public static void logEndResultsProcessing(ResultSet resultSet) {
    performIfPresent(ql -> {
      QueryLogInfo info = ql._queryLogInfos.get(resultSet);
      ql._queryLogInfos.remove(resultSet);
      if (info != null) {
        ql.logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, System.currentTimeMillis(), false);
      }
      ql.logOrphanedResultSets();
    });
  }

  public static void submitTimer(SqlTimer timer) {
    performIfPresent(ql -> {
      Long[] times = timer.getTimes();
      ql.logQueryTime(timer.getSql(), timer.getSqlName(), 0,
          times[SqlTimerEvents.SQL_EXECUTED.ordinal()],
          times[SqlTimerEvents.COMPLETE.ordinal()], false);
    });
  }

  private void logQueryTime(String sql, String name, long startTime, long firstPageTime, long completionTime, boolean isLeak) {

    double lastPageSeconds = (completionTime - startTime) / 1000D;
    double firstPageSeconds = firstPageTime < 0 ? lastPageSeconds : (firstPageTime - startTime) / 1000D;
    String details = String.format(" first: %8.3f last: %8.3f [%s] %s", firstPageSeconds, lastPageSeconds, name, (isLeak? " LEAK " : ""));

    if (lastPageSeconds < 0 || firstPageSeconds < 0) {
      LOG.error("code error, negative exec time:" + details);
      new Exception().printStackTrace();
    }
    // convert the time to seconds, then log time & sql for slow query. goes to warn log
    if (lastPageSeconds >= _config.getSlow() && !_config.isIgnoredSlow(sql)) {
      SlowQueryLog.getLogger().warn("SLOW QUERY LOG" + details + "\n" + sql);
    }

    // log time for baseline query, and only sql for the first time goes to info log
    else if (lastPageSeconds >= _config.getBaseline() && !_config.isIgnoredBaseline(sql)) {
      SlowQueryLog.getLogger().warn("     QUERY LOG" + details);

      synchronized (_queryNames) {
        if (!_queryNames.contains(name)) {
          _queryNames.add(name);
          ExampleQueryLog.getLogger().info("EXAMPLE QUERY" + details + "\n" + sql);
        }
      }
    }
  }

  /**
   * Log orphaned result sets, ie, those that are closed but still in hash
   */
  private void logOrphanedResultSets() {
    Map<ResultSet,QueryLogInfo> closedResultSets = null; 
    synchronized ( _queryLogInfos ){
      if (_queryLogInfos.size() != 0) {
        closedResultSets = new HashMap<>(); 
        Set<ResultSet> resultSets = new HashSet<>(_queryLogInfos.keySet()); 
        for (ResultSet rs : resultSets) {
          QueryLogInfo info = _queryLogInfos.get(rs);
          // consider a leak if open for more than an hour
          if (System.currentTimeMillis() - info.startTime > 1000 * 3600) {
            closedResultSets.put(rs, _queryLogInfos.get(rs));
            _queryLogInfos.remove(rs);
          }
        }
      }
    }
    if (closedResultSets != null) {
      for (ResultSet rs : closedResultSets.keySet()) {
        QueryLogInfo info = closedResultSets.get(rs);
        logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, System.currentTimeMillis(), true);
      }
    }
  }

  /**
   * Helper class containing data about a single SQL execution
   */
  private static class QueryLogInfo {
    String sql;
    String name;
    long startTime;
    long firstPageTime;

    public QueryLogInfo(String sql, String name, long startTime, long firstPageTime) {
      this.sql = sql;
      this.name = name;
      this.startTime = startTime;
      this.firstPageTime = firstPageTime;
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
