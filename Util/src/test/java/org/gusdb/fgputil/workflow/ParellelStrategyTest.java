package org.gusdb.fgputil.workflow;

import org.gusdb.fgputil.workflow.Node;
import org.junit.Test;

public class ParellelStrategyTest {

  @Test
  public void testParelleStrategyRetrieval() throws Exception {
    Node root = getNodeTree();
    root.runWithDependencies();
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
  
  //@Test(expected = WorkflowException.class)
  public void testCircularDependencyCheck() throws Exception {
    
  }
  
}
