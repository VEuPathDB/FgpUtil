package org.gusdb.fgputil.runtime;

public class UnfetchableInstanceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnfetchableInstanceException(String message, Exception cause) {
    super(message, cause);
  }
}
