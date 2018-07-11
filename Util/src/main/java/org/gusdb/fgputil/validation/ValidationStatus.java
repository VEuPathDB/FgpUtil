package org.gusdb.fgputil.validation;

public enum ValidationStatus {

  // NOTE: the ordinals of these values are important; see ValidationBundle.aggregateStatus()
  UNVALIDATED,         // start state
  FAILED_SYNTAX,       // syntax checked and failed
  SYNTACTICALLY_VALID, // syntax checked and valid (but semantics not checked yet)
  FAILED_SEMANTICS,    // syntax valid but semantics check failed
  SEMANTICALLY_VALID;  // syntax and semantics checked and valid

  public boolean isValidated() {
    return !equals(UNVALIDATED);
  }

  public boolean isValid() {
    return equals(SYNTACTICALLY_VALID) || equals(SEMANTICALLY_VALID);
  }

}
