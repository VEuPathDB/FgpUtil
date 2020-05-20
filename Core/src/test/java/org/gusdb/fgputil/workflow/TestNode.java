package org.gusdb.fgputil.workflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.workflow.Node;

public class TestNode extends Node {

  private static Logger LOG = Logger.getLogger(TestNode.class);
  
  private static ExecutorService _execService = Executors.newFixedThreadPool(30);
  
  private String _id;
  private long _duration;
  
  public TestNode(String id, long duration) {
    LOG.info("Creating node with " + id + ", " + duration);
    _id = id;
    _duration = duration;
  }

  @Override
  public void doWork() throws Exception {
    LOG.info("Starting Node " + _id);
    Thread.sleep(1000 * _duration);
    LOG.info("Finishing Node " + _id);
  }

  @Override
  public ExecutorService getExecutorService() {
    return _execService;
  }
  
  @Override
  public String toString() {
    return "Node " + _id + ", " + _duration;
  }

  @Override
  protected void receiveData(Node fromNode, Object data) {
    // we don't expect any data from child nodes; ignore
  }
}
