package org.gusdb.fgputil.validation;

import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
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

  public default boolean isValidAtLevelGreaterThanOrEqualTo(ValidationLevel targetLevel) {
    return isValid() && getValidationBundle().getLevel().isGreaterThanOrEqualTo(targetLevel);
  }

  public default boolean isDisplayablyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.DISPLAYABLE);
  }

  public default boolean isSyntacticallyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.SYNTACTIC);
  }

  public default boolean isSemanticallyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.SEMANTIC);
  }

  public default boolean isRunnable() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.RUNNABLE);
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<DisplayablyValid<T>, T> getDisplayablyValid() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getDisaplayablyValid(value));
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<SyntacticallyValid<T>, T> getSyntacticallyValid() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getSyntacticallyValid(value));
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<SemanticallyValid<T>, T> getSemanticallyValid() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getSemanticallyValid(value));
  }

  @SuppressWarnings("unchecked")
  public default OptionallyInvalid<RunnableObj<T>, T> getRunnable() {
    return optionallyValidOnException((T)this, value -> ValidObjectFactory.getRunnable(value));
  }

  static <S extends Valid<T>, T extends Validateable<T>> OptionallyInvalid<S,T>
  optionallyValidOnException(T value, FunctionWithException<T,S> converter) {
    try {
      S wrappedValue = converter.apply(value);
      return new OptionallyInvalid<S,T>(wrappedValue, null);
    }
    catch (Exception e) {
      return new OptionallyInvalid<S,T>(null, value);
    }
  }
}
