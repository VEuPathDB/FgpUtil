package org.gusdb.fgputil.db.wrapper;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.wrapper.UnclosedObjectInfo.CloseableObjectType;

public class DataSourceWrapper extends AbstractDataSourceWrapper {

  private static final Logger LOG = Logger.getLogger(DataSourceWrapper.class);

  private final String _dbName;
  private final DBPlatform _underlyingPlatform;
  private final Map<Connection, UnclosedObjectInfo> _unclosedConnectionMap = new ConcurrentHashMap<>();
  private final Map<String, String> _globalStacktraceMap = new ConcurrentHashMap<>();
  private final AtomicInteger _numConnectionsOpened = new AtomicInteger(0);
  private final AtomicInteger _numConnectionsClosed = new AtomicInteger(0);
  private final boolean _recordAllStacktraces;
  private final ConnectionPoolConfig _dbConfig;

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource,
      DBPlatform underlyingPlatform, ConnectionPoolConfig dbConfig) {
    this(dbName, underlyingDataSource, underlyingPlatform, dbConfig, false);
  }

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource, DBPlatform underlyingPlatform,
      ConnectionPoolConfig dbConfig, boolean recordAllStacktraces) {
    super(underlyingDataSource);
    _dbName = dbName;
    _underlyingPlatform = underlyingPlatform;
    _dbConfig = dbConfig;
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

  public ConnectionPoolConfig getDbConfig() {
    return _dbConfig;
  }

  private Connection wrapConnection(Connection conn) {
    UnclosedObjectInfo info = (_recordAllStacktraces ?
      new UnclosedObjectInfo(_dbName, CloseableObjectType.Connection, _globalStacktraceMap) :
      new UnclosedObjectInfo(_dbName, CloseableObjectType.Connection));
    if (LOG.isDebugEnabled()) {
      // log hash for this connection; let caller know which connection was opened
      LOG.debug("Opening connection associated with stacktrace hash " +
          info.getStackTraceHash() + " : " + info.getBasicInfo());
    }
    ConnectionWrapper wrapper = new ConnectionWrapper(conn, this, _underlyingPlatform);
    _unclosedConnectionMap.put(conn, info);
    _numConnectionsOpened.incrementAndGet();
    return wrapper;
  }

  public void unregisterClosedConnection(Connection conn) {
    if (LOG.isDebugEnabled()) {
      // log hash for this connection; let caller know which connection was closed
      UnclosedObjectInfo info = _unclosedConnectionMap.get(conn);
      LOG.debug("Closing connection associated with stacktrace hash " +
      info.getStackTraceHash() + " : " + info.getBasicInfo());
    }
    _numConnectionsClosed.incrementAndGet();
    _unclosedConnectionMap.remove(conn);
  }

  public String dumpUnclosedObjectInfo() {
    
    // accumulate counts of stack traces
    Collection<UnclosedObjectInfo> rawInfoList = _unclosedConnectionMap.values();
    Map<String, List<UnclosedObjectInfo>> countsMap = new HashMap<>();
    List<List<UnclosedObjectInfo>> countsList = new ArrayList<>();

    // getting map values should be thread safe; values are simple pojos
    for (UnclosedObjectInfo info : rawInfoList) {
      String hash = info.getStackTraceHash();
      List<UnclosedObjectInfo> counts = countsMap.get(hash);
      if (counts == null) {
        counts = new ArrayList<UnclosedObjectInfo>();
        countsMap.put(hash, counts);
        countsList.add(counts);
      }
      counts.add(info);
    }

    // sort by number of instances (descending)
    Collections.sort(countsList, new Comparator<List<UnclosedObjectInfo>>() {
      @Override
      public int compare(List<UnclosedObjectInfo> o1, List<UnclosedObjectInfo> o2) {
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

      for (List<UnclosedObjectInfo> infoList : countsList) {
        UnclosedObjectInfo firstInfo = infoList.get(0);
        sb.append("  ").append(infoList.size()).append(" : ").append(firstInfo.getStackTraceHash()).append(NL);
      }

      sb.append(NL)
        .append("======================").append(NL)
        .append(" Unclosed Connections ").append(NL)
        .append("======================").append(NL).append(NL);
      for (List<UnclosedObjectInfo> infoList : countsList) {
        UnclosedObjectInfo firstInfo = infoList.get(0);
        sb.append(firstInfo.getStackTraceHash()).append(": ")
          .append(infoList.size()).append(" instances").append(NL).append(NL)
          .append("  Instance details:").append(NL).append(NL);
        for (UnclosedObjectInfo info : infoList) {
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

}
