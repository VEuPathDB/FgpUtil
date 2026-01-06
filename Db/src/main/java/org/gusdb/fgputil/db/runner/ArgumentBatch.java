package org.gusdb.fgputil.db.runner;

/**
 * Enables access to multiple sets of arguments, in the event that the caller
 * wishes to execute a batch operation.
 *
 * @author rdoherty
 */
public interface ArgumentBatch extends Iterable<Object[]> {
  /**
   * Tells SQLRunner how many instructions to add before executing a batch
   *
   * @return how many instructions should be added before executing a batch
   */
  public int getBatchSize();

  /**
   * Tells SQLRunner what type of data is being submitted for each parameter.
   * Please use values from java.sql.Types.  A value of null for a given
   * param tells SQLRunner to intelligently 'guess' the type for that param.
   * A value of null returned by this method tells SQLRunner to guess for all
   * params.  Note guessing is less efficient.
   *
   * @return SQL types that will suggest the type of data to be passed, or
   * null if SQLRunner is to guess the types.
   */
  public Integer[] getParameterTypes();
}
