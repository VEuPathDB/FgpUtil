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
  ValidationBundle getValidationBundle();

  /**
   * By default, returns the response of the validation bundle's status's isValid()
   *
   * @return whether this object is valid
   */
  default boolean isValid() {
    return getValidationBundle().getStatus().isValid();
  }

  default boolean isValidAtLevelGreaterThanOrEqualTo(ValidationLevel targetLevel) {
    return isValid() && getValidationBundle().getLevel().isGreaterThanOrEqualTo(targetLevel);
  }

  default boolean isDisplayablyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.DISPLAYABLE);
  }

  default boolean isSyntacticallyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.SYNTACTIC);
  }

  default boolean isSemanticallyValid() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.SEMANTIC);
  }

  default boolean isRunnable() {
    return isValidAtLevelGreaterThanOrEqualTo(ValidationLevel.RUNNABLE);
  }

  @SuppressWarnings("unchecked")
  default OptionallyInvalid<DisplayablyValid<T>, T> getDisplayablyValid() {
    return optionallyValidOnException((T)this, ValidObjectFactory::getDisaplayablyValid);
  }

  @SuppressWarnings("unchecked")
  default OptionallyInvalid<SyntacticallyValid<T>, T> getSyntacticallyValid() {
    return optionallyValidOnException((T)this, ValidObjectFactory::getSyntacticallyValid);
  }

  @SuppressWarnings("unchecked")
  default OptionallyInvalid<SemanticallyValid<T>, T> getSemanticallyValid() {
    return optionallyValidOnException((T)this, ValidObjectFactory::getSemanticallyValid);
  }

  @SuppressWarnings("unchecked")
  default OptionallyInvalid<RunnableObj<T>, T> getRunnable() {
    return optionallyValidOnException((T)this, ValidObjectFactory::getRunnable);
  }

  static <S extends Valid<T>, T extends Validateable<T>> OptionallyInvalid<S,T>
  optionallyValidOnException(T value, FunctionWithException<T,S> converter) {
    try {
      return new OptionallyInvalid<>(converter.apply(value), null);
    }
    catch (Exception e) {
      return new OptionallyInvalid<>(null, value);
    }
  }
}
