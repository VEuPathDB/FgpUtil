package org.gusdb.fgputil.workflow;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.junit.Test;

public class TopologicalSortingTest {

  private static class Param implements DependencyElement<Param> {

    private final String _name;
    private final String _expectedDepList;
    private final Set<Param> _dependedParams = new HashSet<>();
    private List<Param> _dependentParams = new ArrayList<>();

    public Param(String name, String expectedDepList) {
      _name = name;
      _expectedDepList = expectedDepList;
    }

    @Override
    public String getKey() {
      return _name;
    }

    public void addDependedParams(Param... params) {
      _dependedParams.addAll(Arrays.asList(params));
    }

    @Override
    public Set<Param> getDependedElements() {
      return _dependedParams;
    }

    @Override
    public void setDependentElements(List<Param> dependentElements) {
      _dependentParams = dependentElements;
    }

    public String getDependentElementsAsString() {
      return FormatUtil.join(_dependentParams.toArray(), ",");
    }

    @Override
    public String toString() {
      return _name;
    }

    public String getExpectedDepList() {
      return _expectedDepList;
    }
  }

  @Test
  public void testSorting() throws Exception {

    Param A = new Param("A", "C,B,D,E");
    Param B = new Param("B", "D,E");
    Param C = new Param("C", "B,D,E");
    Param D = new Param("D", "E");
    Param E = new Param("E", "");
    Param F = new Param("F", "");

    Param[] allParams = { A, B, C, D, E, F };

    // A does not depend on anyone
    B.addDependedParams(A,C);
    C.addDependedParams(A);
    D.addDependedParams(B);
    E.addDependedParams(A,C,D);
    // F does not depend on anyone

    DependencyResolver<Param> resolver = new DependencyResolver<>();
    resolver.addElements(allParams);
    List<Param> ordering = resolver.resolveDependencyOrder();

    List<String> orderingStr = resolver.convertToKeyList(ordering);
    System.out.println("Full ordered list: " + orderingStr);
    assertEquals("[F, A, C, B, D, E]", orderingStr.toString());
    for (Param param : allParams) {
      System.out.println("Param " + param.getKey() + ": [" + param.getDependentElementsAsString() + "]");
      assertEquals(param.getExpectedDepList(), param.getDependentElementsAsString());
    }
  }
}
