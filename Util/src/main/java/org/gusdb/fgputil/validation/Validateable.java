package org.gusdb.fgputil.validation;

/**
 * Simple interface to add to classes that use this validation package to demonstrate
 * whether they have been validated or not and that validation status.
 * 
 * @author rdoherty
 */
public interface Validateable {

  /**
   * Returns a validation bundle containing validation information about this object
   * @return a validation bundle containing validation information about this object
   */
  public ValidationBundle getValidationBundle();

}
