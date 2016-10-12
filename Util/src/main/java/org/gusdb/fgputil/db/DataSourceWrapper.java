package org.gusdb.fgputil.db;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.platform.DBPlatform;

public class DataSourceWrapper implements DataSource {

  // must use fully qualified Logger name since java.util Logger is part of the interface
  private static final org.apache.log4j.Logger LOG =
      org.apache.log4j.Logger.getLogger(DataSourceWrapper.class);

  private static class UnclosedConnectionInfo {

    private String _dbName;
    private Date _timeOpened;
    private String _stackTrace;
    private String _stackTraceHash;

    public UnclosedConnectionInfo(String dbName) {
      this(dbName, null);
    }

    public UnclosedConnectionInfo(String dbName, Map<String, String> globalStacktraceMap) {
      _dbName = dbName;
      _timeOpened = new Date();
      _stackTrace = FormatUtil.getCurrentStackTrace();
      _stackTraceHash = EncryptionUtil.encrypt(_stackTrace);
      // only add stack trace to global map if specified
      if (globalStacktraceMap != null) {
        globalStacktraceMap.put(_stackTraceHash, _stackTrace);
      }
    }

    public String getStackTraceHash() {
      return _stackTraceHash;
    }

    public String getStackTrace() {
      return _stackTrace;
    }

    public String getBasicInfo() {
      String timeOpenedStr = new SimpleDateFormat(DATE_FORMAT).format(_timeOpened);
      double secondsOpen = ((double)(new Date().getTime() - _timeOpened.getTime())) / 1000;
      return new StringBuilder()
          .append("Connection to ").append(_dbName)
          .append(", open for ").append(secondsOpen)
          .append(" seconds, retrieved from pool at ").append(timeOpenedStr)
          .toString();
    }

    @Override
    public String toString() {
      return new StringBuilder()
        .append(getStackTraceHash()).append(": ")
        .append(getBasicInfo()).append(NL)
        .append(getStackTrace())
        .toString();
    }
  }

  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  private final String _dbName;
  private final DataSource _underlyingDataSource;
  private final DBPlatform _underlyingPlatform;
  private final Map<Connection, UnclosedConnectionInfo> _unclosedConnectionMap = new ConcurrentHashMap<>();
  private final Map<String, String> _globalStacktraceMap = new ConcurrentHashMap<>();
  private final AtomicInteger _numConnectionsOpened = new AtomicInteger(0);
  private final AtomicInteger _numConnectionsClosed = new AtomicInteger(0);
  private final boolean _recordAllStacktraces;
  private final boolean _autoCommitResetValue;
  private final boolean _readOnlyResetValue;

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource, DBPlatform underlyingPlatform,
      boolean autoCommitResetValue, boolean readOnlyResetValue) {
    this(dbName, underlyingDataSource, underlyingPlatform, autoCommitResetValue, readOnlyResetValue, false);
  }

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource, DBPlatform underlyingPlatform,
      boolean autoCommitResetValue, boolean readOnlyResetValue, boolean recordAllStacktraces) {
    _dbName = dbName;
    _underlyingDataSource = underlyingDataSource;
    _underlyingPlatform = underlyingPlatform;
    _autoCommitResetValue = autoCommitResetValue;
    _readOnlyResetValue = readOnlyResetValue;
    _recordAllStacktraces = recordAllStacktraces;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return wrapConnection(_underlyingDataSource.getConnection());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return wrapConnection(_underlyingDataSource.getConnection(username, password));
  }

  private Connection wrapConnection(Connection conn) {
    UnclosedConnectionInfo info = (_recordAllStacktraces ?
      new UnclosedConnectionInfo(_dbName, _globalStacktraceMap) :
      new UnclosedConnectionInfo(_dbName));
    if (LOG.isDebugEnabled()) {
      // log hash for this connection; let caller know which connection was opened
      LOG.debug("Opening connection associated with stacktrace hash " +
          info.getStackTraceHash() + " : " + info.getBasicInfo());
    }
    ConnectionWrapper wrapper = new ConnectionWrapper(conn, this,
        _underlyingPlatform, _autoCommitResetValue, _readOnlyResetValue);
    _unclosedConnectionMap.put(conn, info);
    _numConnectionsOpened.incrementAndGet();
    return wrapper;
  }

  public void unregisterClosedConnection(Connection conn) {
    if (LOG.isDebugEnabled()) {
      // log hash for this connection; let caller know which connection was closed
      UnclosedConnectionInfo info = _unclosedConnectionMap.get(conn);
      LOG.debug("Closing connection associated with stacktrace hash " +
      info.getStackTraceHash() + " : " + info.getBasicInfo());
    }
    _numConnectionsClosed.incrementAndGet();
    _unclosedConnectionMap.remove(conn);
  }

  public String dumpUnclosedConnectionInfo() {
    
    // accumulate counts of stack traces
    Collection<UnclosedConnectionInfo> rawInfoList = _unclosedConnectionMap.values();
    Map<String, List<UnclosedConnectionInfo>> countsMap = new HashMap<>();
    List<List<UnclosedConnectionInfo>> countsList = new ArrayList<>();

    // getting map values should be thread safe; values are simple pojos
    for (UnclosedConnectionInfo info : rawInfoList) {
      String hash = info.getStackTraceHash();
      List<UnclosedConnectionInfo> counts = countsMap.get(hash);
      if (counts == null) {
        counts = new ArrayList<UnclosedConnectionInfo>();
        countsMap.put(hash, counts);
        countsList.add(counts);
      }
      counts.add(info);
    }

    // sort by number of instances (descending)
    Collections.sort(countsList, new Comparator<List<UnclosedConnectionInfo>>() {
      @Override
      public int compare(List<UnclosedConnectionInfo> o1, List<UnclosedConnectionInfo> o2) {
        return o2.size() - o1.size();
      }
    });

    // build output
    StringBuilder sb = new StringBuilder(NL)
        .append("================================").append(NL)
        .append(" Unclosed Connection Statistics ").append(NL)
        .append("================================").append(NL).append(NL)
        .append("  ").append(_numConnectionsOpened.get()).append(" connections opened").append(NL)
        .append("  ").append(_numConnectionsClosed.get()).append(" connections closed").append(NL)
        .append("  ").append(rawInfoList.size()).append(" currently open connections").append(NL).append(NL);

    // if no unclosed connections exist, skip unclosed section
    if (!rawInfoList.isEmpty()) {

      for (List<UnclosedConnectionInfo> infoList : countsList) {
        UnclosedConnectionInfo firstInfo = infoList.get(0);
        sb.append("  ").append(infoList.size()).append(" : ").append(firstInfo.getStackTraceHash()).append(NL);
      }

      sb.append(NL)
        .append("======================").append(NL)
        .append(" Unclosed Connections ").append(NL)
        .append("======================").append(NL).append(NL);
      for (List<UnclosedConnectionInfo> infoList : countsList) {
        UnclosedConnectionInfo firstInfo = infoList.get(0);
        sb.append(firstInfo.getStackTraceHash()).append(": ")
          .append(infoList.size()).append(" instances").append(NL).append(NL)
          .append("  Instance details:").append(NL).append(NL);
        for (UnclosedConnectionInfo info : infoList) {
          sb.append("    ").append(info.getBasicInfo()).append(NL);
        }
        sb.append(NL)
          .append("  Stack trace:").append(NL).append(NL)
          .append("    ").append(firstInfo.getStackTrace()).append(NL);
      }
    }

    // show entire mapping of hash -> stacktrace
    if (_recordAllStacktraces) {
      sb.append(NL)
        .append("================================").append(NL)
        .append(" Historical Stacktrace Hash Map ").append(NL)
        .append("================================").append(NL).append(NL)
        .append(_globalStacktraceMap.size())
        .append(" distinct stack traces opened connections since initialization.")
        .append(NL).append(NL);
      for (Entry<String, String> hashMapping : _globalStacktraceMap.entrySet()) {
        sb.append(hashMapping.getKey()).append(NL).append("  ")
          .append(hashMapping.getValue()).append(NL).append(NL);
      }
    }

    return sb.toString();
  }
 
  public int getNumConnectionsOpened() {
    return _numConnectionsOpened.get();
  }

  public int getNumConnectionsClosed() {
    return _numConnectionsClosed.get();
  }

  public int getConnectionsCurrentlyOpen() {
    return _unclosedConnectionMap.values().size();
  }

  /************ ALL METHODS BELOW THIS LINE ARE SIMPLE WRAPPERS ************/

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return _underlyingDataSource.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    _underlyingDataSource.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    _underlyingDataSource.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return _underlyingDataSource.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return _underlyingDataSource.getParentLogger();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingDataSource.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingDataSource.isWrapperFor(iface);
  }

}
