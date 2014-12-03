package org.gusdb.fgputil.workflow;

import java.util.List;
import java.util.Set;

public interface DependencyElement<T extends DependencyElement<?>> {

  /**
   * Returns an identifier for an element, distinct among a group of elements
   * 
   * @return element identifier
   */
  public String getKey();

  /**
   * Returns set of elements this element depends on
   * 
   * @return set of elements this element depends on
   */
  public Set<T> getDependedElements();

  /**
   * Sets a topologically ordered list of elements
   * 
   * @param dependentElements an ordered list of elements dependent
   * on this one; this topological ordering guarantees no
   * double-execution (i.e. if an inter-dependency exists, later
   * elements depend on earlier elements)
   */
  public void setDependentElements(List<T> dependentElements);
}
