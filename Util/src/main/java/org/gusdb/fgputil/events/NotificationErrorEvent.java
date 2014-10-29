package org.gusdb.fgputil.events;

public class NotificationErrorEvent extends Event {

  public static final String EVENT_CODE = "__notification_error__";

  private final EventListener _listener;
  private final Event _event;
  private final Exception _exception;
  
  public NotificationErrorEvent(EventListener listener, Event event, Exception exception) {
    super(EVENT_CODE);
    _listener = listener;
    _event = event;
    _exception = exception;
  }

  public EventListener getListener() { return _listener; }
  public Event getEvent() { return _event; }
  public Exception getException() { return _exception; }

}
