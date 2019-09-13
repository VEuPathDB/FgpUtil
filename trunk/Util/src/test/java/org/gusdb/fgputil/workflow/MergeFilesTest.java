package org.gusdb.fgputil.workflow;

import org.junit.Test;

public class MergeFilesTest {

	@Test
	public void testFlowNode() {
		TestFlowNode flow = new TestFlowNode.TrunkNode();
		flow.addDependency(new TestFlowNode.ResultsMergeNode()
			  .addDependency(new TestFlowNode.FileMergeNode("file1.txt", "file2.txt"))
		      .addDependency(new TestFlowNode.FileMergeNode("file3.txt", "file4.txt")))
		    .addDependency(new TestFlowNode.ResultsMergeNode()
		      .addDependency(new TestFlowNode.FileMergeNode("file5.txt", "file6.txt"))
		      .addDependency(new TestFlowNode.FileMergeNode("file7.txt", "file8.txt")));
		flow.run();
	}
	
}
