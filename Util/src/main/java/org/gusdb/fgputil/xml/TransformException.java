package org.gusdb.fgputil.xml;

public class TransformException extends Exception {

  private static final long serialVersionUID = 1L;

  public TransformException(String message) {
    super(message);
  }

  public TransformException(Exception cause) {
    super(cause);
  }

  public TransformException(String message, Exception cause) {
    super(message, cause);
  }

}
