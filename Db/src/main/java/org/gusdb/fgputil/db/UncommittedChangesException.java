package org.gusdb.fgputil.db;

public class UncommittedChangesException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UncommittedChangesException(String message) {
    super(message);
  }

}
