package org.gusdb.fgputil.db.slowquery;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.db.slowquery.QueryLogger.ExampleQueryLog;
import org.gusdb.fgputil.db.slowquery.QueryLogger.SlowQueryLog;
import org.gusdb.fgputil.db.slowquery.SqlTimer.SqlTimerEvents;
import org.gusdb.fgputil.db.slowquery.SqlTimer.SqlTimerLogger;

public class QueryLoggerInstance implements SqlTimerLogger {

  private static final Logger LOG = Logger.getLogger(QueryLoggerInstance.class);

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

  private final QueryLogConfig _config;
  private final Set<String> _queryNames = new HashSet<>();
  private final Map<ResultSet, QueryLogInfo> _queryLogInfos =
      Collections.synchronizedMap(new HashMap<ResultSet, QueryLogInfo>());

  public QueryLoggerInstance(QueryLogConfig config) {
    _config = config;
  }

  public void logStartResultsProcessing(String sql, String name,
      long startTime, ResultSet resultSet) {
    QueryLogInfo info = new QueryLogInfo(sql, name, startTime, System.currentTimeMillis());
    _queryLogInfos.put(resultSet,info);
  }

  public void logEndResultsProcessing(ResultSet resultSet) {
    QueryLogInfo info = _queryLogInfos.get(resultSet);
    _queryLogInfos.remove(resultSet);
    if (info != null) {
      logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, System.currentTimeMillis(), false);
    }
  }

  public void logQueryTime(String sql, String name, long startTime, long firstPageTime, long completionTime, boolean isLeak) {

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

  @Override
  public void submitTimer(SqlTimer timer) {
    String queryName = EncryptionUtil.encrypt(timer.getSql());
    Long[] times = timer.getTimes();
    logQueryTime(timer.getSql(), queryName, 0,
        times[SqlTimerEvents.SQL_EXECUTED.ordinal()],
        times[SqlTimerEvents.COMPLETE.ordinal()], false);
  }

  public void logOrphanedResultSets() {
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
        logQueryTime(info.sql, info.name, info.startTime, info.firstPageTime, System.currentTimeMillis(), true);
      }
    }
  }
}
