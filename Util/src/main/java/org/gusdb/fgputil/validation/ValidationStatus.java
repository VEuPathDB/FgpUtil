package org.gusdb.fgputil.validation;

public enum ValidationStatus {

  UNVALIDATED,         // start state
  SYNTACTICALLY_VALID, // syntax checked and valid (but semantics not checked yet)
  FAILED_SYNTAX,       // syntax checked and failed
  SEMANTICALLY_VALID,  // syntax and semantics checked and valid
  FAILED_SEMANTICS;    // syntax valid but semantics check failed

  public boolean isValidated() {
    return !equals(UNVALIDATED);
  }

  public boolean isValid() {
    return equals(SYNTACTICALLY_VALID) || equals(SEMANTICALLY_VALID);
  }

}
