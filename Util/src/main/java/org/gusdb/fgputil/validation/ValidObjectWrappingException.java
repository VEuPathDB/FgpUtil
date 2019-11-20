package org.gusdb.fgputil.validation;

import org.gusdb.fgputil.FormatUtil;

/**
 * Typically thrown when code attempts to create a Valid wrapper around an invalid object.
 * 
 * @author rdoherty
 */
public class ValidObjectWrappingException extends RuntimeException {

  private static final String DEFAULT_MESSAGE = "Attempt made to wrap invalid or unvalidated " +
      "object in a Valid wrapper.  This is almost certainly a bug in the calling code.";

  public ValidObjectWrappingException(ValidationBundle validation) {
    super(DEFAULT_MESSAGE + FormatUtil.NL + validation.toString());
  }

}
