package org.gusdb.fgputil.workflow;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

/**
 * General purpose mechanism for multi-threaded workflow system.  This class
 * provides a base for building a dependency tree of tasks which can be run
 * in a multi-threaded fashion, using the ExecutorService of the implementer's
 * choice.
 * 
 * @author rdoherty
 */
public abstract class Node implements Runnable {

  private static final Logger LOG = Logger.getLogger(Node.class);
  
  /** Enumeration of the possible states of this node */
  public enum RunStatus {
    STANDBY, WAITING, RUNNING, ERROR, UNRUN, COMPLETE;
  }
  
  private volatile RunStatus _status = RunStatus.STANDBY;
  private volatile int _numCompleteDeps;

  private Set<Node> _dependencies = new HashSet<>();
  private Set<Node> _parents = new HashSet<>();

  /**
   * The implementation of this method should execute the work associated with
   * a given task.
   * 
   * @throws Exception if an error occurs in processing
   */
  public abstract void doWork() throws Exception;
  
  /**
   * Implementations must provide the ExecutorService from which this node will
   * execute dependencies.
   * 
   * @return the preferred implementation of ExecutorService
   */
  public abstract ExecutorService getExecutorService();
  
  /**
   * Add the passed node as a dependency of this node.
   * 
   * @param node depended node
   * @return this, the dependent node
   */
  public Node addDependency(Node node) {
    _dependencies.add(node);
    node.addParent(this);
    checkForCircularDependencies();
    return this;
  }

  private void addParent(Node node) {
    _parents.add(node);
  }

  private void checkForCircularDependencies() throws IllegalArgumentException {
    // TODO Auto-generated method stub
  }

  private void notifyComplete(Node node) {
    synchronized(this) {
      _numCompleteDeps++;
      if (node.getStatus().equals(RunStatus.ERROR)) {
        _status = RunStatus.ERROR;
      }
    }
  }

  /**
   * Kicks off the dependencies of this node, waits for them to complete, then
   * runs this node and notifies its parent(s).
   */
  @Override
  public void run() {
    runDependencies();
    runSelf();
    for (Node parent : _parents) {
      parent.notifyComplete(this);
    }
  }
  
  /**
   * @return the current status of this node
   */
  public RunStatus getStatus() {
    return _status;
  }
  
  private void setStatus(RunStatus status) {
    LOG.info("Status change: " + this + ": From " + _status + " to " + status);
    _status = status;
  }
  
  /**
   * Runs this node, handles errors thrown by implementations, assigns status
   * at each step.
   */
  private void runSelf() {
    try {
      if (_status.equals(RunStatus.ERROR)) {
        setStatus(RunStatus.UNRUN);
      }
      else {
        setStatus(RunStatus.RUNNING);
        doWork();
        setStatus(RunStatus.COMPLETE);
      }
    }
    catch (Exception e) {
      LOG.error(e);
      setStatus(RunStatus.ERROR);
    }
  }

  /**
   * Runs the dependencies of this node and waits for all of them to complete.
   * Note "complete" in this context means either successful or unsuccessful
   * completion.
   */
  private void runDependencies() {
    _numCompleteDeps = 0;
    setStatus(RunStatus.WAITING);
    for (Node node : _dependencies) {
      getExecutorService().execute(node);
    }
    while (true) {
      if (_numCompleteDeps == _dependencies.size()) {
        break;
      }
    }
  }
}
