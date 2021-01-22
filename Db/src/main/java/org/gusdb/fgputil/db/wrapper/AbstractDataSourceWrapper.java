package org.gusdb.fgputil.db.wrapper;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AbstractDataSourceWrapper implements DataSource {

  protected final DataSource _underlyingDataSource;

  protected AbstractDataSourceWrapper(DataSource underlyingDataSource) {
    _underlyingDataSource = underlyingDataSource;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingDataSource.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingDataSource.isWrapperFor(iface);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return _underlyingDataSource.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return _underlyingDataSource.getConnection(username, password);
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return _underlyingDataSource.getParentLogger();
  }

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

}
