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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;

public class DataSourceWrapper implements DataSource {

  // must use fully qualified Logger name since java.util Logger is part of the interface
  private static final org.apache.log4j.Logger LOG =
      org.apache.log4j.Logger.getLogger(DataSourceWrapper.class);
  
  private static class UnclosedConnectionInfo {

    private String _dbName;
    private Date _timeOpened;
    private String _stackTrace;

    public UnclosedConnectionInfo(String dbName) {
      _dbName = dbName;
      _timeOpened = new Date();
      _stackTrace = FormatUtil.getCurrentStackTrace();
    }

    public String getStackTraceHash() {
      return EncryptionUtil.encryptNoCatch(_stackTrace);
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
  private final Map<Connection, UnclosedConnectionInfo> _unclosedConnectionMap = new ConcurrentHashMap<>();
  
  public DataSourceWrapper(String dbName, DataSource underlyingDataSource) {
    _dbName = dbName;
    _underlyingDataSource = underlyingDataSource;
  }

  public Connection getConnection() throws SQLException {
    return wrapConnection(_underlyingDataSource.getConnection());
  }

  public Connection getConnection(String username, String password) throws SQLException {
    return wrapConnection(_underlyingDataSource.getConnection(username, password));
  }

  private Connection wrapConnection(Connection conn) {
    ConnectionWrapper wrapper = new ConnectionWrapper(conn, this);
    _unclosedConnectionMap.put(conn, new UnclosedConnectionInfo(_dbName));
    return wrapper;
  }

  public void unregisterClosedConnection(Connection conn) {
    if (LOG.isDebugEnabled()) {
      // log hash for this connection; let caller know which connection was closed
      UnclosedConnectionInfo info = _unclosedConnectionMap.get(conn);
      LOG.debug("Closing connection associated with stacktrace hash " +
      info.getStackTraceHash() + " : " + info.getBasicInfo());
    }
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
      public int compare(List<UnclosedConnectionInfo> o1, List<UnclosedConnectionInfo> o2) {
        return o2.size() - o1.size();
      }
    });

    // build output
    StringBuilder sb = new StringBuilder(NL)
        .append("Unclosed Connection Statistics (").append(rawInfoList.size())
        .append(" total open connections):").append(NL).append(NL);
    for (List<UnclosedConnectionInfo> infoList : countsList) {
      UnclosedConnectionInfo firstInfo = infoList.get(0);
      sb.append("  ").append(infoList.size()).append(" : ").append(firstInfo.getStackTraceHash()).append(NL);
    }
    sb.append(NL).append("Unclosed Connections:").append(NL).append(NL);
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
    return sb.toString();
  }

  /************ ALL METHODS BELOW THIS LINE ARE SIMPLE WRAPPERS ************/

  public PrintWriter getLogWriter() throws SQLException {
    return _underlyingDataSource.getLogWriter();
  }

  public void setLogWriter(PrintWriter out) throws SQLException {
    _underlyingDataSource.setLogWriter(out);
  }

  public void setLoginTimeout(int seconds) throws SQLException {
    _underlyingDataSource.setLoginTimeout(seconds);
  }

  public int getLoginTimeout() throws SQLException {
    return _underlyingDataSource.getLoginTimeout();
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return _underlyingDataSource.getParentLogger();
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingDataSource.unwrap(iface);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingDataSource.isWrapperFor(iface);
  }

}
