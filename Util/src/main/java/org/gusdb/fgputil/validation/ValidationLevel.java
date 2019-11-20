package org.gusdb.fgputil.validation;

/**
 * The level of validation requested or performed.  Note the order of these
 * levels is important since we often compare via ordinals.
 * 
 * @author rdoherty
 */
public enum ValidationLevel {

  /**
   * No validation was, or should be, performed.
   */
  NONE,

  /**
   * Syntactic validation is typically "cheap" to perform (i.e. in-memory only).  Named so since it usually
   * involves only analyzing the structure of an object and perhaps perform simple regex or range validations.
   */
  SYNTACTIC,

  /**
   * Tells whether or not a value or set of values are valid enough to be displayed (e.g. in a form).
   * Typically this is a lower level of validation than even SYNTACTIC since e.g. a string that does not
   * conform to a specified regex can still be displayed in textbox.
   */
  DISPLAYABLE,

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

  public boolean isLessThanOrEqualTo(ValidationLevel level) {
    return ordinal() <= level.ordinal();
  }

  public boolean isGreaterThanOrEqualTo(ValidationLevel level) {
    return ordinal() >= level.ordinal();
  }
}
