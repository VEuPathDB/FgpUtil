package org.gusdb.fgputil.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParellelStrategyTest {

  @Test
  public void testParelleStrategyRetrieval() {
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

  @Test
  public void testCircularDependencyCheck() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
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
        e.printStackTrace();
        throw e;
      }
    });
  }
}
