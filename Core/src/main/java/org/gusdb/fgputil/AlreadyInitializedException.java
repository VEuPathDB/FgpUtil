package org.gusdb.fgputil;

public class AlreadyInitializedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AlreadyInitializedException(String message) {
    super(message);
  }
}
