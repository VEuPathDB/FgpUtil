package org.gusdb.fgputil.validation;

import java.util.Optional;

import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.SyntacticallyValid;

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
  public default Optional<SyntacticallyValid<T>> getSyntacticallyValid() {
    return optionalOnException(() -> ValidObjectFactory.getSyntacticallyValid((T)this));
  }

  public default boolean isSemanticallyValid() {
    ValidationLevel level = getValidationBundle().getLevel();
    return getValidationBundle().getStatus().equals(ValidationStatus.VALID) &&
           (level.equals(ValidationLevel.SEMANTIC) ||
            level.equals(ValidationLevel.RUNNABLE));
  }

  @SuppressWarnings("unchecked")
  public default Optional<SemanticallyValid<T>> getSemanticallyValid() {
    return optionalOnException(() -> ValidObjectFactory.getSemanticallyValid((T)this));
  }

  public default boolean isRunnable() {
    return getValidationBundle().getStatus().equals(ValidationStatus.VALID) &&
           getValidationBundle().getLevel().equals(ValidationLevel.RUNNABLE);
  }

  @SuppressWarnings("unchecked")
  public default Optional<RunnableObj<T>> getRunnable() {
    return optionalOnException(() -> ValidObjectFactory.getRunnable((T)this));
  }

  static <S> Optional<S> optionalOnException(SupplierWithException<S> supplier) {
    try {
      return Optional.of(supplier.supply());
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }
}
