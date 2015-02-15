package org.gusdb.fgputil.db.pool;

public class InitializationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InitializationException(String message) {
    super(message);
  }
  
  public InitializationException(Exception cause) {
    super(cause);
  }
  
  public InitializationException(String message, Exception cause) {
    super(message, cause);
  }
  
}
