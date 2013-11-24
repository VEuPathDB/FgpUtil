package org.gusdb.fgputil.db.runner;

public class SQLRunnerException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public SQLRunnerException(String msg) {
    super(msg);
  }
  
  public SQLRunnerException(String msg, Exception cause) {
    super(msg, cause);
  }

}
