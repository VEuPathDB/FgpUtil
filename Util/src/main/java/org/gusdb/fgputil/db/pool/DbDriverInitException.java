package org.gusdb.fgputil.db.pool;

public class DbDriverInitException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DbDriverInitException(String message) {
    super(message);
  }
  
  public DbDriverInitException(Exception cause) {
    super(cause);
  }
  
  public DbDriverInitException(String message, Exception cause) {
    super(message, cause);
  }
  
}
