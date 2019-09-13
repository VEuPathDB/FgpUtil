package org.gusdb.fgputil.validation;

/**
 * Typically thrown when code attempts to create a Valid wrapper around an invalid object.
 * 
 * @author rdoherty
 */
public class ValidObjectWrappingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final String DEFAULT_MESSAGE = "Attempt made to wrap invalid or unvalidated " +
      "object in a Valid wrapper.  This is almost certainly a bug in the calling code.";

  public ValidObjectWrappingException() {
    super(DEFAULT_MESSAGE);
  }

}
