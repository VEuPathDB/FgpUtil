package org.gusdb.fgputil.cache;

/**
 * Exception to be thrown when an object cannot be fetched by an ItemFetcher.  A
 * typical pattern would be for calling code to catch this exception and throw
 * its cause, catching and handling exceptions representing the various ways
 * a fetch might fail.
 * 
 * @author rdoherty
 */
public class UnfetchableItemException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnfetchableItemException(Exception cause) {
    super(cause);
  }
}
