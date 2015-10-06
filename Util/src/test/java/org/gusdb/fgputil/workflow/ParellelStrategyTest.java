package org.gusdb.fgputil.workflow;

import org.gusdb.fgputil.workflow.Node;
import org.junit.Test;

public class ParellelStrategyTest {

  @Test
  public void testParelleStrategyRetrieval() throws Exception {
    Node root = getNodeTree();
    root.run();
  }

  private Node getNodeTree() {
    return new TestNode("0", 1)
      .addDependency(new TestNode("1a", 2)
        .addDependency(new TestNode("2a",1))
        .addDependency(new TestNode("2b",2)))
      .addDependency(new TestNode("1b", 3)
        .addDependency(new TestNode("2c",1))
        .addDependency(new TestNode("2d",2)))
      .addDependency(new TestNode("1c", 2));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCircularDependencyCheck() throws Exception {
    try {
      Node circDep = new TestNode("X", 1);
      Node root = new TestNode("0", 1)
        .addDependency(new TestNode("1a", 2)
          .addDependency(new TestNode("2a",1))
          .addDependency(new TestNode("2b",2)))
        .addDependency(new TestNode("1b", 3)
          .addDependency(new TestNode("2c",1))
          .addDependency(new TestNode("2d",2).addDependency(circDep)))
        .addDependency(new TestNode("1c", 2));
      circDep.addDependency(root);
    }
    catch (IllegalArgumentException e) {
      System.out.println(e);
      throw e;
    }
  }
  
}
