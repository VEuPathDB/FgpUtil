package org.gusdb.fgputil.db;

import java.sql.SQLException;

/**
 * Provides a RuntimeException wrapper around SQLException
 * 
 * @author rdoherty
 */
public class SqlRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SqlRuntimeException(SQLException e) {
    super(e);
  }

  @Override
  public synchronized SQLException getCause() {
    return (SQLException)super.getCause();
  }
}
