package org.gusdb.fgputil.events;

import java.util.Date;

import org.gusdb.fgputil.FormatUtil;

/**
 * Represents an event in the system.  Typically this class will be
 * extended to hold event-specific data which may be "passed" to the
 * listeners of the event.  If the fact that something simply happened
 * is sufficient, this method can be used as is with a custom event
 * code.  Listeners desiring notification of those events should
 * subscribe to the event code.
 * 
 * @author ryan
 */
public class Event {

  private static final String UNKNOWN_STACKTRACE = "Unknown";

  private final String _eventCode;
  private final Date _creationDate;
  private final String _creationStackTrace;

  /**
   * Creates an event.  Events created using this constructor will
   * return the fully qualified class name as the event code; however
   * subscribing to this class name (as opposed to the class itself)
   * will not result in a notification.  This no-arg constructor is
   * meant only to be called implicitly by subclass constructors.
   */
  protected Event() {
    _eventCode = null;
    _creationDate = new Date();
    _creationStackTrace = Events.isTrackEventCreationStackTraces() ?
        FormatUtil.getCurrentStackTrace() : UNKNOWN_STACKTRACE;
  }

  /**
   * Creates an event with the given event code.  When this event
   * is triggered, listeners for both its type and its event code
   * will be notified (but only once per event, even is subscribed
   * to both type and code).
   * 
   * @param eventCode
   */
  public Event(String eventCode) {
    if (eventCode == null) throw new NullPointerException(
        "Cannot create Event with null event code.");
    _eventCode = eventCode;
    _creationDate = new Date();
    _creationStackTrace = Events.isTrackEventCreationStackTraces() ?
        FormatUtil.getCurrentStackTrace() : UNKNOWN_STACKTRACE;
  }

  /**
   * Returns this event's event code.  If the event does not have
   * a code, the fully qualified classname of this event is returned,
   * but this should not imply that subscribing to the class name
   * (as a String) will result in notifications of events of that type.
   * 
   * @return event code of this event
   */
  public final String getEventCode() {
    return (_eventCode == null ? getClass().getName() : _eventCode);
  }

  /**
   * Returns timestamp of this event's creation.  Note the event might
   * be triggered some time after creation.
   * 
   * @return creation date of this event
   */
  public final Date getCreationDate() {
    return _creationDate;
  }

  /**
   * Returns the stack trace of the event's creation (i.e. the place in
   * code where this event was generated).  By default, this feature is
   * turned off and will return "Unknown".  It can be turned on by calling
   * <code>Events.init</code> with a custom configuration.
   * 
   * @return stack trace of the event's creation
   */
  public final String getCreationStackTrace() {
    return _creationStackTrace;
  }
}
