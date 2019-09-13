package org.gusdb.fgputil.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregates and reports on the current state of a set of threads created
 * to call the <code>eventTriggered</code> method of a set of respective
 * <code>EventListener</code>s.
 * 
 * @see Events#trigger(Event)
 * @author ryan
 */
public class ListenerStatus {

  /**
   * Set of values representing the processing status of a particular event
   * listener, or collectively a list of listeners
   * 
   * @author ryan
   */
  public static enum Status {
    NOTIFIED, SUCCESS, ERRORED;
  }

  private Map<EventListener, Status> _statuses = new ConcurrentHashMap<>();

  ListenerStatus(Set<EventListener> listeners) {
    for (EventListener listener : listeners) {
      _statuses.put(listener, Status.NOTIFIED);
    }
  }

  void notifySuccess(EventListener listener) {
    _statuses.put(listener, Status.SUCCESS);
  }

  void notifyError(EventListener listener) {
    _statuses.put(listener, Status.ERRORED);
  }

  /**
   * Returns the aggregate status of all notified listeners. If
   * all listeners have completed processing successfully, SUCCESS
   * is returned; if all listeners have completed, but one or more
   * threw an exception, ERROR is returned; otherwise, NOTIFIED is
   * returned.
   * 
   * @return aggregate status of listeners of a particular event
   */
  @SuppressWarnings("incomplete-switch")
  public Status getCollectiveStatus() {
    boolean errorPresent = false;
    for (Status status : _statuses.values()) {
      switch (status) {
        case NOTIFIED:
          return Status.NOTIFIED;
        case ERRORED:
          errorPresent = true;
      }
    }
    return (errorPresent ? Status.ERRORED : Status.SUCCESS);
  }

  /**
   * Returns true if all listeners have completed processing, else false.
   * This includes listeners which threw exceptions; this method will only
   * return true after all listeners have either returned or thrown exception.
   * 
   * @return whether all listeners for an event have completed
   */
  public boolean isFinished() {
    Status status = getCollectiveStatus();
    return (status.equals(Status.SUCCESS) || status.equals(Status.ERRORED));
  }
}
