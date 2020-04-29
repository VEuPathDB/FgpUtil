package org.gusdb.fgputil.workflow;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestNode extends Node {

  private static final Logger LOG = Logger.getLogger(TestNode.class);

  private static final ExecutorService _execService = Executors.newFixedThreadPool(30);

  private final String _id;
  private final long _duration;

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
