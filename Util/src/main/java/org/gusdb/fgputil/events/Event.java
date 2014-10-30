package org.gusdb.fgputil.events;

import java.util.Date;

public abstract class Event {
  
  private final String _eventCode;
  private final Date _submittedDate;

  public Event() {
    _eventCode = null;
    _submittedDate = new Date();
  }
  
  public Event(String eventCode) {
    if (eventCode == null) throw new NullPointerException(
        "Cannot create Event with null event code.");
    _eventCode = eventCode;
    _submittedDate = new Date();
  }

  public final String getEventCode() {
    return (_eventCode == null ? getClass().getName() : _eventCode);
  }

  public final Date getSubmittedDate() {
    return _submittedDate;
  }
}
