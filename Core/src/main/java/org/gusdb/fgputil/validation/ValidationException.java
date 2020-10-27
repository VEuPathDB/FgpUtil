package org.gusdb.fgputil.validation;

/**
 * Exception class containing a ValidationBundle
 * 
 * @author rdoherty
 */
public class ValidationException extends Exception {

  private final ValidationBundle _validation;

  public ValidationException(ValidationBundle validation) {
    super(validation.toString());
    _validation = validation;
  }

  public ValidationBundle getValidationBundle() {
    return _validation;
  }

}
