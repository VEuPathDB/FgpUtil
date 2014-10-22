package org.gusdb.fgputil.db;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class QueryLogger {

  private static final Logger logger = Logger.getLogger(QueryLogger.class);
  
  private static boolean _initialized = false;
  private static QueryLogConfig _config;
  private static final Set<String> _queryNames = new HashSet<>();
  private static Map<ResultSet, QueryLogInfo> _queryLogInfos = Collections.synchronizedMap(new HashMap<ResultSet, QueryLogInfo>());

  /**
   * @author Steve Fischer
   * Allow logging of example queries to a separate logger.
   */
  static final class ExampleQueryLog {
    private static final Logger _logger = Logger.getLogger(getInnerClassLog4jName(ExampleQueryLog.class));
    private ExampleQueryLog() {}
    public static Logger getLogger() { return _logger; }
  }
  static final class SlowQueryLog {
    private static final Logger _logger = Logger.getLogger(getInnerClassLog4jName(SlowQueryLog.class));
    private SlowQueryLog() {}
    public static Logger getLogger() { return _logger; }
  }
  
  /**
   * Log4j only accepts logger names using dot delimiters, but Class.getName()
   * returns "package.InnerClass$OuterClass", which is not referenceable by
   * the name attribute of a logger tag in log4j.xml.  This function gives a
   * name usable by both.
   * 
   * @param clazz inner class name
   * @return the "code-style" inner class name
   */
  private static String getInnerClassLog4jName(Class<?> clazz) {
	  return clazz.getName().replace("$", ".");
  }
  
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

  public static synchronized void initialize(QueryLogConfig config) {
    if (_initialized) {
      logger.warn("Multiple calls to initialize().  Ignoring...");
    } else {
      logger.info("Initializing QueryLogger (slow and example query logs)");
      ExampleQueryLog.getLogger().debug("Initializing example query log");
      SlowQueryLog.getLogger().debug("Initializing slow query log");
      _config = config;
      _initialized = true;
    }
  }

  private static void checkInit() {
    if (!_initialized) {
      throw new IllegalStateException("The init() method must be called on this class before any other method is called.");
    }
  }
  
  /** 
   * Call this version to track and log query time if you are using a ResultSet.  Call it after the execute but before iterating through the resultSet.
   * When done with the result set use one of SqlUtil's close methods that take a ResultSet argument to close it.
   */
  public static void logStartResultsProcessing(String sql, String name,
        long startTime, ResultSet resultSet) {
    checkInit();
    QueryLogInfo info = new QueryLogInfo(sql, name, startTime, System.currentTimeMillis());
    _queryLogInfos.put(resultSet,info);
  }

  public static void logEndResultsProcessing(ResultSet resultSet) {
    checkInit();
    QueryLogInfo info = _queryLogInfos.get(resultSet);
    _queryLogInfos.remove(resultSet);
    if (info != null) {
      logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, false);
    }
  }

  /** 
   * Call this version to track and log query time if you do not have a resultSet, eg, for an update or insert.  Call it after the execute.
   */
  public static void logEndStatementExecution(String sql, String name, long startTime) {
    checkInit();
    logQueryTime(sql, name, startTime, -1, false);
  }

  public static void logOrphanedResultSets() {
    checkInit();
    Map<ResultSet,QueryLogInfo> closedResultSets = null; 
    synchronized ( _queryLogInfos ){
      if (_queryLogInfos.size() != 0) {
        closedResultSets = new HashMap<ResultSet, QueryLogInfo>(); 
        Set<ResultSet> resultSets = new HashSet<ResultSet>(_queryLogInfos.keySet()); 
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
        logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, true);
      }
    }
  }

  private static void logQueryTime(String sql, String name,
           long startTime, long firstPageTime, boolean isLeak) {
    /* we now have long dataset names, the length validation isn't appropriate now.
    if (name.length() > 100 || name.indexOf('\n') >= 0) {
      StringWriter writer = new StringWriter();
      new Exception().printStackTrace(new PrintWriter(writer));
      logger.warn("The name of the sql is suspicious, name: '" + name
      + "', trace:\n" + writer.toString());
    }*/

    double lastPageSeconds = (System.currentTimeMillis() - startTime) / 1000D;
    double firstPageSeconds = firstPageTime < 0? lastPageSeconds : (firstPageTime - startTime) / 1000D;
 
    String details = " [" + name + "] execute: " + firstPageSeconds + " last page: " + lastPageSeconds + " seconds" + (isLeak? " LEAK " : "");
    if (lastPageSeconds < 0 || firstPageSeconds < 0) {
      logger.error("code error, negative exec time:" + details);
      new Exception().printStackTrace();
    }
    // convert the time to seconds
    // log time & sql for slow query. goes to warn log
    if (lastPageSeconds >= _config.getSlow() && !_config.isIgnoredSlow(sql)) {
      SlowQueryLog.getLogger().warn("SLOW QUERY LOG" + details + "\n" + sql);
    }

    // log time for baseline query, and only sql for the first time. goes to
    // info log
    else if (lastPageSeconds >= _config.getBaseline() && !_config.isIgnoredBaseline(sql)) {
      SlowQueryLog.getLogger().warn("QUERY LOG" + details);

      synchronized (_queryNames) {
        if (!_queryNames.contains(name)) {
          _queryNames.add(name);
          ExampleQueryLog.getLogger().info("EXAMPLE QUERY" + details + "\n" + sql);
        }
      }
    }
  }
}
