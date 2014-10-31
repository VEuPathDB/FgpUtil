package org.gusdb.fgputil.events;

import static org.junit.Assert.assertEquals;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.events.CompletionStatus.Status;
import org.junit.Test;

public class EventsTest implements EventListener {

  private static final String THROW_ERROR = "throw_error";

  public static class TestEvent extends Event {

    public static final String TEST_EVENT_CODE = "__test_event_code__";

    private String _details;

    public TestEvent(String details) {
      super(TEST_EVENT_CODE);
      _details = details;
    }

    public String getDetails() {
      return _details;
    }
  }

  @Test
  public void testEvents() throws Exception {
    try {
      Events.init();
      Events.subscribe(this, TestEvent.TEST_EVENT_CODE);
      Events.subscribe(this, NotificationErrorEvent.class);
      Events.subscribe(new EventListener() {
        @Override public void notifyEvent(Event event) throws Exception {
          System.out.println("Event submitted with code: " + event.getEventCode());
        }}, Event.class);
      Status status = getStatusWhenSubmitting("some message");
      assertEquals(status, Status.SUCCESS);
      status = getStatusWhenSubmitting(THROW_ERROR);
      assertEquals(status, Status.ERRORED);
    }
    finally {
      Events.shutDown();
    }
  }

  private Status getStatusWhenSubmitting(String message) {
    CompletionStatus status = Events.submit(new TestEvent(message));
    while (!status.isFinished()) {
      System.out.println("Checking completion: " + status.getCollectiveStatus());
      sleep(20);
    }
    Status statusEnum = status.getCollectiveStatus();
    System.out.println("Listeners are finished with status: " + statusEnum);
    return statusEnum;
  }

  @Override
  public void notifyEvent(Event event) throws Exception {
    System.out.println("Received event with code '" + event.getEventCode() + "' of type: " + event.getClass().getName());
    if (event instanceof TestEvent) {
      sleep(100);
      TestEvent testEvent = (TestEvent)event;
      System.out.println("It contains details like: " + testEvent.getDetails());
      if (testEvent.getDetails().equals(THROW_ERROR)) {
        throw new Exception("Problem!");
      }
    }
    else if (event instanceof NotificationErrorEvent) {
      NotificationErrorEvent errorEvent = (NotificationErrorEvent)event;
      System.out.println("Handling error event. Class " + errorEvent.getListener().getClass().getName() +
          " received event class " + errorEvent.getEvent().getClass().getName() +
          " and threw Exception:" + FormatUtil.NL + FormatUtil.getStackTrace(errorEvent.getException()));
    }
  }

  private void sleep(int ms) {
    try { Thread.sleep(ms); } catch (InterruptedException e) {}
  }
}
