package org.gusdb.fgputil.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CompletionStatus {

  public static enum Status {
    NOTIFIED, COMPLETE, ERRORED;
  }

  private Map<EventListener, Status> _statuses = new ConcurrentHashMap<>();

  public CompletionStatus(Set<EventListener> listeners) {
    for (EventListener listener : listeners) {
      _statuses.put(listener, Status.NOTIFIED);
    }
  }

  public void notifyComplete(EventListener listener) {
    _statuses.put(listener, Status.COMPLETE);
  }

  public void notifyError(EventListener listener) {
    _statuses.put(listener, Status.ERRORED);
  }

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
    return (errorPresent ? Status.ERRORED : Status.COMPLETE);
  }

  public boolean isFinished() {
    Status status = getCollectiveStatus();
    return (status.equals(Status.COMPLETE) || status.equals(Status.ERRORED));
  }
}
