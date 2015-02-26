package org.gusdb.fgputil.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.events.ListenerStatus.Status;
import org.junit.Test;

public class EventsTest implements EventListener {

  private static final String THROW_ERROR = "throw_error";

  private static int totalEventsReceived = 0;

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
      // initialize events framework with default configuration
      Events.init();

      // subscribe to events
      Events.subscribe(this, TestEvent.TEST_EVENT_CODE);
      Events.subscribe(this, ListenerExceptionEvent.class);
      Events.subscribe(new EventListener() {
        @Override public void eventTriggered(Event event) throws Exception {
          totalEventsReceived++;
          System.out.println("Event submitted with code: " + event.getEventCode());
        }}, Event.class);

      // trigger an event where we expect all listeners to succeed
      Status status = getStatusWhenSubmitting("some message");
      assertEquals(status, Status.SUCCESS);

      // trigger an event where we expect a listener to fail
      status = getStatusWhenSubmitting(THROW_ERROR);
      assertEquals(status, Status.ERRORED);

      // trigger an event were we expect all listeners to succed using triggerAndWait()
      try {
        Events.triggerAndWait(new TestEvent("some message"), new Exception("failed!"));
        assertTrue(true);
      }
      catch(Exception e) {
        assertTrue(false);
      }

      // trigger an event were we expect all listeners to succed using triggerAndWait()
      try {
        Events.triggerAndWait(new TestEvent(THROW_ERROR), new Exception("failed!"));
        assertTrue(false);
      }
      catch(Exception e) {
        assertTrue(true);
      }

      // wait for error events to be received; since error events are simply
      //   triggered (not waited for), the number-received check below sometimes
      //   fails without yielding to the error event processing threads
      // NOTE: this is still a race condition and may fail occasionally!!
      sleep(2000);

      // make sure the correct number of events have been processed
      assertEquals(12, totalEventsReceived);
    }
    finally {
      Events.shutDown();
    }
  }

  private Status getStatusWhenSubmitting(String message) {
    ListenerStatus status = Events.trigger(new TestEvent(message));
    while (!status.isFinished()) {
      System.out.println("Checking completion: " + status.getCollectiveStatus());
      sleep(20);
    }
    Status statusEnum = status.getCollectiveStatus();
    System.out.println("Listeners are finished with status: " + statusEnum);
    return statusEnum;
  }

  @Override
  public void eventTriggered(Event event) throws Exception {
    totalEventsReceived++;
    System.out.println("Received event with code '" + event.getEventCode() + "' of type: " + event.getClass().getName());
    if (event instanceof TestEvent) {
      sleep(100);
      TestEvent testEvent = (TestEvent)event;
      System.out.println("It contains details like: " + testEvent.getDetails());
      if (testEvent.getDetails().equals(THROW_ERROR)) {
        throw new Exception("Problem!");
      }
    }
    else if (event instanceof ListenerExceptionEvent) {
      ListenerExceptionEvent errorEvent = (ListenerExceptionEvent)event;
      System.out.println("Handling error event. Class " + errorEvent.getListener().getClass().getName() +
          " received event class " + errorEvent.getEvent().getClass().getName() +
          " and threw Exception:" + FormatUtil.NL + FormatUtil.getStackTrace(errorEvent.getException()));
    }
  }

  private void sleep(int ms) {
    try { Thread.sleep(ms); } catch (InterruptedException e) {}
  }
}
