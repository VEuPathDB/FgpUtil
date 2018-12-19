package org.gusdb.fgputil.validation;

import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.SyntacticallyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.Valid;

/**
 * Simple interface to add to classes that use this validation package to demonstrate
 * whether they have been validated or not and that validation status.
 * 
 * @author rdoherty
 */
public interface Validateable<T extends Validateable<T>> {

  /**
   * Returns a validation bundle containing validation information about this object
   * 
   * @return a validation bundle containing validation information about this object
   */
  public ValidationBundle getValidationBundle();

  /**
   * By default, returns the response of the validation bundle's status's isValid()
   * 
   * @return whether this object is valid
   */
  public default boolean isValid() {
    return getValidationBundle().getStatus().isValid();
  }

  public default boolean isSyntacticallyValid() {
    ValidationLevel level = getValidationBundle().getLevel();
    return getValidationBundle().getStatus().equals(ValidationStatus.VALID) &&
           (level.equals(ValidationLevel.SYNTACTIC) ||
            level.equals(ValidationLevel.SEMANTIC) ||
            level.equals(ValidationLevel.RUNNABLE));
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<T, SyntacticallyValid<T>> getSyntacticallyValid() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getSyntacticallyValid(value));
  }

  public default boolean isSemanticallyValid() {
    ValidationLevel level = getValidationBundle().getLevel();
    return getValidationBundle().getStatus().equals(ValidationStatus.VALID) &&
           (level.equals(ValidationLevel.SEMANTIC) ||
            level.equals(ValidationLevel.RUNNABLE));
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<T, SemanticallyValid<T>> getSemanticallyValid() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getSemanticallyValid(value));
  }

  public default boolean isRunnable() {
    return getValidationBundle().getStatus().equals(ValidationStatus.VALID) &&
           getValidationBundle().getLevel().equals(ValidationLevel.RUNNABLE);
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<T, RunnableObj<T>> getRunnable() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getRunnable(value));
  }

  static <T extends Validateable<T>, S extends Valid<T>> OptionallyInvalid<T,S>
  optionallyValidOnException(T value, FunctionWithException<T,S> converter) {
    try {
      S wrappedValue = converter.apply(value);
      return new OptionallyInvalid<T,S>(wrappedValue, null);
    }
    catch (Exception e) {
      return new OptionallyInvalid<T,S>(null, value);
    }
  }
}
