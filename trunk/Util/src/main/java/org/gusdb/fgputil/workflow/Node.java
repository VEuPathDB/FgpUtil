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
    /** Node had been created but not yet run */
    STANDBY,
    /** Node is kicking off dependencies or is waiting for them to complete */
    WAITING,
    /** Node's dependencies have completed and it is now running */
    RUNNING,
    /** Error has occurred while node was running */
    ERROR,
    /** One of the node's dependencies has erred so this node will not be run */
    UNRUN,
    /** Node and all its dependencies have completed successfully */
    COMPLETE;
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
   * Allows nodes to pass information back and forth through a standard
   * interface.
   * 
   * @param fromNode node sending the data
   * @param data data object
   */
  protected abstract void receiveData(Node fromNode, Object data);
  
  /**
   * Add the passed node as a dependency of this node.
   * 
   * @param node depended node
   * @return this, the dependent node
   */
  public Node addDependency(Node node) {
    _dependencies.add(node);
    node.addParent(this);
    checkForCircularDependencies(node);
    return this;
  }

  protected void addParent(Node node) {
    _parents.add(node);
  }

  protected Set<Node> getDependencies() {
    return _dependencies;
  }
  
  protected void sendToParents(Object data) {
    for (Node parent : _parents) {
      parent.receiveData(this, data);
    }
  }
  
  /**
   * Search logic:
   *   1. Assume at time newDependency is added we have no circular dependencies
   *   2. We are saying that we depend on this new node now
   *   3. Make sure new node doesn't depend on us (or transitively depend on us)
   * 
   * @param newDependency the new dependency
   * @throws IllegalArgumentException if new dependency introduces a circular dependency
   */
  private void checkForCircularDependencies(Node newDependency) throws IllegalArgumentException {
    if (newDependency.findDependency(this)) {
      throw new IllegalArgumentException("Cannot add dependency (" + newDependency +
          ") because it is already dependent on this node (" + this + ")");
    }
  }
  
  /**
   * Recursive function returns true if candidate is a dependency of this node, else false
   * 
   * @param candidate possible dependency
   * @return true if candidate is a dependency, else false
   */
  private boolean findDependency(Node candidate) {
    for (Node dep: getDependencies()) {
      if (dep == candidate || dep.findDependency(candidate)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * This method is called on a dependent node by a depended node when the
   * depended node is complete or erred.
   * 
   * @param node depended node that is notifying this node of its completion
   */
  private void notifyComplete(Node node) {
    synchronized(this) {
      _numCompleteDeps++;
      if (node.getStatus().equals(RunStatus.ERROR) ||
          node.getStatus().equals(RunStatus.UNRUN)) {
        _status = RunStatus.UNRUN;
      }
    }
  }

  protected Set<Node> getParents() {
    return _parents;
  }
  
  protected void notifyComplete() {
    for (Node parent : _parents) {
      parent.notifyComplete(this);
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
    notifyComplete();
  }
  
  /**
   * Runs this node, handles errors thrown by implementations, assigns status
   * at each step.
   */
  protected void runSelf() {
    try {
      if (!getStatus().equals(RunStatus.UNRUN)) {
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
  protected void runDependencies() {
    resetCompleteDependencies();
    setStatus(RunStatus.WAITING);
    for (Node node : getDependencies()) {
      getExecutorService().execute(node);
    }
    while (true) {
      if (allDependenciesComplete()) {
        break;
      }
    }
  }
  
  protected void resetCompleteDependencies() {
    _numCompleteDeps = 0;
  }
  
  protected boolean allDependenciesComplete() {
    return _numCompleteDeps == _dependencies.size();
  }
  
  /**
   * Returns the current state of this node
   * 
   * @return the current status of this node
   */
  public RunStatus getStatus() {
    return _status;
  }
  
  protected void setStatus(RunStatus status) {
    LOG.info("Status change: " + this + ": From " + _status + " to " + status);
    _status = status;
  }
}
