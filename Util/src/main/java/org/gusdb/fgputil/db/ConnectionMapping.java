package org.gusdb.fgputil.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class ConnectionMapping {

  private static final Map<Connection, Connection> CONNECTION_MAP = new ConcurrentHashMap<>();

  /**
   * Returns a connection from the given data source.  If the connection is
   * a <code>WrappedConnection</code>, adds the underlying connection to a
   * mapping so the wrapped connection can later be retrieved and closed properly.
   * 
   * @param dataSource data source from which to fetch a connection
   * @return connection from the data source
   * @throws SQLException if unable to get connection from data source
   */
  public static Connection getConnection(DataSource dataSource) throws SQLException {
    Connection conn = dataSource.getConnection();
    if (conn instanceof ConnectionWrapper) {
      ConnectionWrapper wrapper = (ConnectionWrapper)conn;
      CONNECTION_MAP.put(wrapper.getUnderlyingConnection(), wrapper);
    }
    return conn;
  }

  /**
   * Retrieves the parent connection for the given statement.  If the
   * connection found maps to a wrapped connection, the wrapper is
   * returned and the mapping is removed.  NOTE: this means future calls
   * to this method will NOT return the WrappedConnection; the one
   * returned first must be closed or it will show as a leak
   * 
   * @param stmt statement for which to find a parent connection
   * @return proper parent connection for this statement
   * @throws SQLException if unable to get connection from statement
   */
  public static Connection getConnection(Statement stmt) throws SQLException {
    Connection conn = stmt.getConnection();
    if (CONNECTION_MAP.containsKey(conn)) {
      // connection received from statement has a wrapper
      Connection underlyingConnection = conn;
      // get wrapper from map and assign to conn for return
      conn = CONNECTION_MAP.get(conn);
      // remove mapping; it is assumed returned connection will be closed
      CONNECTION_MAP.remove(underlyingConnection);
    }
    return conn;
  }

}
