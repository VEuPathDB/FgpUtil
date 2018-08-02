package org.gusdb.fgputil.validation;

/**
 * The level of validation requested or performed.
 * 
 * @author rdoherty
 */
public enum ValidationLevel {

  /**
   * No validation was, or should be, performed.
   */
  NONE,

  /**
   * Validation level is unknown; typically this is used when the calling code does not know what level of
   * validation the called code is going to perform.  The called code is responsible for choosing a validation
   * level and setting the validation bundle's status accordingly.
   */
  UNSPECIFIED,

  /**
   * Syntactic validation is typically "cheap" to perform (i.e. in-memory only).  Named so since it usually
   * involves only analyzing the structure of an object and perhaps perform simple regex or range validations.
   */
  SYNTACTIC,

  /**
   * Semantic validation includes syntactic validation but adds validation that is typically more expensive
   * (i.e. may require DB calls, possibly with dependencies).  Named so since it usually involves looking up
   * vocabularies of allowable values for comparison.  Objects that pass semantic validation are highly
   * unlikely to throw errors when used.
   */
  SEMANTIC,

  /**
   * Runnable validation is typically equivalent to semantic validation except on objects which can be "run"
   * e.g. an AnswerSpec or QueryInstanceSpec, in which case not only must an object be semantically valid, but
   * any objects it depends on to run must also be semantically valid, all the way down the dependency tree.
   */
  RUNNABLE;

  /**
   * @return true if validation level is NONE
   */
  public boolean isNone() {
    return equals(NONE);
  }
}
