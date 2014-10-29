package org.gusdb.fgputil.events;

import java.util.Date;

public class Event {
  
  private final String _eventCode;
  private final Date _submittedDate;

  public Event(String eventCode) {
    _eventCode = eventCode;
    _submittedDate = new Date();
  }

  public String getEventCode() {
    return _eventCode;
  }

  public Date getSubmittedDate() {
    return _submittedDate;
  }
}
