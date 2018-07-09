package org.gusdb.fgputil.validation;

/**
 * The level of validation requested or performed.
 * 
 * @author rdoherty
 */
public enum ValidationLevel {

  /**
   * Syntactic validation is typically "cheap" to perform (i.e. in-memory only).  Named so since it usually
   * involves only analyzing the structure of an object and perhaps perform simple regex or range validations.
   */
  SYNTACTIC,

  /**
   * Semantic validation is typically more expensive (i.e. may require DB calls, possibly with dependencies).
   * Named so since it usually involves looking up vocabularies of allowable values for comparison.  Objects
   * that pass semantic validation are highly unlikely to throw errors when used.
   */
  SEMANTIC;
}
