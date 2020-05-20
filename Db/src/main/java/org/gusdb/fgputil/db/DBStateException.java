package org.gusdb.fgputil.db;

import java.sql.SQLException;

/**
 * Indicates that the database is not in the state that we expect it to be.  For
 * example, if a table is not populated, but we expect it to be, this exception
 * may be thrown.
 * 
 * @author rdoherty
 */
public class DBStateException extends SQLException {

  private static final long serialVersionUID = 1L;

  public DBStateException(String message) {
    super(message);
  }
}
