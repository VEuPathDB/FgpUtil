package org.gusdb.fgputil.cache;

/**
 * Exception to be thrown when an object cannot be created or updated by a
 * <code>ValueFactory</code>.  A typical pattern would be for calling code to
 * catch this exception and throw its cause, catching and handling exceptions
 * that represent the various ways value production might fail in their case.
 * 
 * @author rdoherty
 */
public class ValueProductionException extends Exception {

  private static final long serialVersionUID = 1L;

  public ValueProductionException(Exception cause) {
    super(cause);
  }

  public ValueProductionException(String message) {
    super(message);
  }
}
