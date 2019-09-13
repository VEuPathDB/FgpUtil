package org.gusdb.fgputil.events;

/**
 * Event thrown when a listener fails to complete successfully.  Since the
 * ListenerStatus class only reports whether listeners fail or succeed as
 * a group, and doesn't provide details, listening for this event is the
 * only way to capture exceptions that occur when listeners fail.
 * 
 * @see ListenerStatus#getCollectiveStatus()
 * @author ryan
 */
public class ListenerExceptionEvent extends Event {

  private final EventListener _listener;
  private final Event _event;
  private final Exception _exception;

  ListenerExceptionEvent(EventListener listener, Event event, Exception exception) {
    _listener = listener;
    _event = event;
    _exception = exception;
  }

  /**
   * Returns the listener object that threw the exception
   * 
   * @return the listener object that threw the exception
   */
  public EventListener getListener() { return _listener; }

  /**
   * Returns the event passed to the listener, causing it to throw the exception
   * 
   * @return the event passed to the listener, causing it to throw the exception
   */
  public Event getEvent() { return _event; }

  /**
   * Returns the exception thrown
   * 
   * @return the exception thrown
   */
  public Exception getException() { return _exception; }

}
