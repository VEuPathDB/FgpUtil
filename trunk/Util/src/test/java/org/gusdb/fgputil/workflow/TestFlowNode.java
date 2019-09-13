package org.gusdb.fgputil.workflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gusdb.fgputil.IoUtil;

/**
 * This is an attempt to execute a tree structure of methods in parellel, with
 * the leaves of the tree supplying initial data and each branch node merging it
 * in parellel and passing it along to its parent node.  This design was
 * experimental and done while Steve pursued the C/pipe-based unix process
 * solution (rather than threads in a JVM).  It does not work, and does not have
 * a clear path forward.  Feel free to contribute/add.  Done in coordination
 * with MergeFilesTest.java (i.e. defined that API and trying to write this code
 * to make it work).
 * 
 * @author rdoherty
 */
public class TestFlowNode extends Node {

	private static ExecutorService _execService = Executors.newFixedThreadPool(30);
	
	public static class TrunkNode extends TestFlowNode {
		@Override
		protected void receiveData(Node fromNode, Object data) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class ResultsMergeNode extends TestFlowNode {
		@Override
		protected void receiveData(Node fromNode, Object data) {
			
		}
	}
	
	public static class FileMergeNode extends TestFlowNode {

		private final String _file1, _file2;
		
		public FileMergeNode(String file1, String file2) {
			_file1 = file1;
			_file2 = file2;
		}
		
		@Override
		public void doWork() throws Exception {
			BufferedReader reader1 = null, reader2 = null;
			try {
				reader1 = new BufferedReader(new FileReader(_file1));
				reader2 = new BufferedReader(new FileReader(_file2));
				boolean done = false;
				Integer reader1int = null, reader2int = null;
				while (!done) {
					if (reader1int == null && reader1.ready()) {
						reader1int = getIntFromString(reader1.readLine());
					}
					if (reader2int == null && reader2.ready()) {
						reader2int = getIntFromString(reader2.readLine());
					}
					if (reader1int == null && reader2 == null) {
						done = true;
						continue;
					}
					if (reader2int == null || reader1int.compareTo(reader2int) > 0) {
						sendToParents(reader1int);
						reader1int = null;
					}
					else {
						sendToParents(reader2int);
						reader2int = null;
					}
				}
			} catch (IOException e) {
				// ?????
			}
			finally {
				IoUtil.closeQuietly(reader1, reader2);
			}
		}
		
		private static Integer getIntFromString(String s) {
			if (s == null) return null;
			return Integer.parseInt(s);
		}
		
	}

	@Override
	public ExecutorService getExecutorService() {
		return _execService;
	}

	@Override
	public void doWork() throws Exception {
		// do nothing; may be overridden by subclass
	}

	@Override
	protected void receiveData(Node fromNode, Object data) {
		// do nothing; may be overridden by subclass
	}

}
