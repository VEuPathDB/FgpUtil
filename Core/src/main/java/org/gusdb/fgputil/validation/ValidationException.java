package org.gusdb.fgputil.validation;

/**
 * Exception class containing a ValidationBundle
 * 
 * @author rdoherty
 */
public class ValidationException extends Exception {

  private final ValidationBundle _validation;


  public ValidationException(String error) {
    _validation = ValidationBundle
      .builder(ValidationLevel.RUNNABLE)
      .addError(error)
      .build();
  }

  public ValidationException(ValidationBundle validation) {
    _validation = validation;
  }

  @Override
  public String getMessage() {
    return _validation.toString();
  }

  public ValidationBundle getValidationBundle() {
    return _validation;
  }

}
