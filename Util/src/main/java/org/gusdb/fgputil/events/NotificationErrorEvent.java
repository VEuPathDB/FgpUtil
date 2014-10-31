package org.gusdb.fgputil.events;

public class NotificationErrorEvent extends Event {

  private final EventListener _listener;
  private final Event _event;
  private final Exception _exception;
  
  public NotificationErrorEvent(EventListener listener, Event event, Exception exception) {
    _listener = listener;
    _event = event;
    _exception = exception;
  }

  public EventListener getListener() { return _listener; }
  public Event getEvent() { return _event; }
  public Exception getException() { return _exception; }

}
