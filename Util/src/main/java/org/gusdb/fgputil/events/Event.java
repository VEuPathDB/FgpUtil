package org.gusdb.fgputil.events;

import java.util.Date;

import org.gusdb.fgputil.FormatUtil;

public class Event {

  private static final String UNKNOWN_CALLSTACK = "Unknown";

  private final String _eventCode;
  private final Date _submittedDate;
  private final String _sourceCallStack;

  public Event() {
    _eventCode = null;
    _submittedDate = new Date();
    _sourceCallStack = Events.isTrackEventCallStacks() ?
        FormatUtil.getCurrentStackTrace() : UNKNOWN_CALLSTACK;
  }

  public Event(String eventCode) {
    if (eventCode == null) throw new NullPointerException(
        "Cannot create Event with null event code.");
    _eventCode = eventCode;
    _submittedDate = new Date();
    _sourceCallStack = Events.isTrackEventCallStacks() ?
        FormatUtil.getCurrentStackTrace() : UNKNOWN_CALLSTACK;
  }

  public final String getEventCode() {
    return (_eventCode == null ? getClass().getName() : _eventCode);
  }

  public final Date getSubmittedDate() {
    return _submittedDate;
  }

  public final String getSourceCallStack() {
    return _sourceCallStack;
  }
}
