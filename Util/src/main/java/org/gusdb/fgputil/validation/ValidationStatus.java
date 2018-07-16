package org.gusdb.fgputil.validation;

public enum ValidationStatus {

  UNVALIDATED, // start state
  FAILED,
  VALID;

  public boolean isValidated() {
    return !equals(UNVALIDATED);
  }

  public boolean isValid() {
    return equals(VALID);
  }

}
