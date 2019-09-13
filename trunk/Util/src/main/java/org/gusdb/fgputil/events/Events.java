package org.gusdb.fgputil.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.events.ListenerStatus.Status;

/**
 * Static class that provides primary access to event-related
 * functionality.  This class is used to trigger and subscribe
 * to events, and handles maintenance of the subscription registry
 * and asynchronous delivery of events to subscribed listeners.
 * 
 * @author ryan
 */
public class Events {

  /*%%%%%%%%%%%%%% STATIC MEMBERS %%%%%%%%%%%%%%*/

  private static final Logger LOG = Logger.getLogger(Events.class);

  private static final EventsConfig DEFAULT_EVENTS_CONFIG = new EventsConfig() {
    @Override public int getThreadPoolSize() { return 20; }
    @Override public boolean isTrackEventCreationStackTraces() { return false; }
    @Override public String toString() {
      return "Default Events Config { threadPoolSize = " + getThreadPoolSize() +
          ", trackEventCallStacks? " + isTrackEventCreationStackTraces() + " }";
    }
  };

  // singleton Events object
  private static Events EVENTS;

  // access lock for singleton
  private static ReadWriteLock EVENTS_LOCK = new ReentrantReadWriteLock();

  /**
   * Initializes the events framework with the default configuration.
   * 
   * @return true if events configured in this call; false if events
   * framework had already been configured (possibly with different configuration)
   */
  public static final boolean init() {
    return init(DEFAULT_EVENTS_CONFIG);
  }

  /**
   * Initializes the events framework with the passed configuration
   * 
   * @param config configuration object
   * @return true if events configured in this call; false if events
   * framework had already been configured (possibly with different configuration)
   */
  public static boolean init(EventsConfig config) {
    try {
      EVENTS_LOCK.writeLock().lock();
      if (EVENTS == null) {
        EVENTS = new Events(config);
        return true;
      }
      else {
        LOG.warn("Events already initialized with config: " +
            FormatUtil.NL + EVENTS.getConfig().toString());
        return false;
      }
    }
    finally {
      EVENTS_LOCK.writeLock().unlock();
    }
  }

  /**
   * Shuts down the events framework.  Once this method is called,
   * attempts to trigger or subscribe to events will throw exceptions.
   * All previously subscribed listeners are cleared (so their memory
   * can be reclaimed); however the pool of threads responsible for
   * notifying listeners uses only a "best effort" attempt (typically
   * <code>Thread.interrupt</code>) to stop the listener threads.  The
   * events framework can be reinitialized with a new configuration
   * using <code>Events.init</code>.
   */
  public static synchronized void shutDown() {
    try {
      EVENTS_LOCK.writeLock().lock();
      if (EVENTS != null) {
        EVENTS.stop();
        EVENTS = null;
      }
    }
    finally {
      EVENTS_LOCK.writeLock().unlock();
    }
  }

  /**
   * Subscribes the passed listener to events that have any of the passed
   * event codes.  When an event is triggered whose getEventCode() method
   * returns any of the exact (case-sensitive) codes, the listener's
   * eventTriggered() method is called with the event.  Multiple calls to
   * this method with the same arguments do not subscribe twice; a listener
   * will only ever receive an event one time.
   * 
   * @param listener listener to subscribe
   * @param eventCodes event codes to subscribe to
   */
  public static void subscribe(EventListener listener, String... eventCodes) {
    try {
      EVENTS_LOCK.readLock().lock();
      checkInit();
      for (String eventCode : eventCodes) {
        EVENTS.addListener(eventCode, listener);
      }
    }
    finally {
      EVENTS_LOCK.readLock().unlock();
    }
  }

  /**
   * Subscribes the passed listener to events of the passed type.  This will
   * include child types (e.g. subscribing to <code>org.gusdb.fgputil.events.Event</code>
   * will subscribe to all events).  When an event is triggered of any of the
   * passed types (or subtypes), the listener's eventTriggered() method is called
   * with the event.  Multiple calls to this method with the same arguments do not
   * subscribe twice; a listener will only ever receive an event one time.
   * 
   * @param listener listener to subscribe
   * @param eventTypes event types to subscribe to
   */
  @SafeVarargs
  public static void subscribe(EventListener listener, Class<? extends Event>... eventTypes) {
    try {
      EVENTS_LOCK.readLock().lock();
      checkInit();
      for (Class<? extends Event> eventType : eventTypes) {
        EVENTS.addListener(eventType, listener);
      }
    }
    finally {
      EVENTS_LOCK.readLock().unlock();
    }
  }

  /**
   * Unsubscribes the passed listener from any events it is currently subscribed to.
   * If the listener is a "temporary" object, this method should be called before the
   * object passes out of scope or is closed/cleared to prevent a memory leak.  The
   * reference inside the Events framework is NOT a weak reference and thus will keep
   * the listener object on the heap unless this method is called.
   * 
   * @param listener listener to unsubscribe
   */
  public static void unsubscribe(EventListener listener) {
    try {
      EVENTS_LOCK.readLock().lock();
      checkInit();
      EVENTS.removeListener(listener);
    }
    finally {
      EVENTS_LOCK.readLock().unlock();
    }
  }

  /**
   * Triggers the passed event.  Any listeners subscribed to
   * this event type, or to events with this event's code will
   * be notified asynchronously via their
   * <code>EventListener.eventTriggered</code> method.  This
   * method will not wait for listeners of this event to
   * complete; the triggerer can choose to wait for them and
   * act based on their collective success or failure based on
   * information received from the <code>CompletionStatus</code>
   * object returned.
   * 
   * @param event event to trigger
   * @return listener result aggregator for this event
   */
  public static ListenerStatus trigger(Event event) {
    try {
      EVENTS_LOCK.readLock().lock();
      checkInit();
      return EVENTS.submitEvent(event);
    }
    finally {
      EVENTS_LOCK.readLock().unlock();
    }
  }

  /**
   * Triggers the passed event.  Any listeners subscribed to
   * this event type, or to events with this event's code will
   * be notified asynchronously via their
   * <code>EventListener.eventTriggered</code> method.  This
   * method will not wait for listeners of this event to
   * complete; the triggerer can choose to wait for them and
   * act based on their collective success or failure based on
   * information received from the <code>CompletionStatus</code>
   * object returned.
   * 
   * @param <T> type of exception passed, parameterized so calling code must only catch the exception it passes
   * @param event event to trigger
   * @param exceptionToThrow exception to throw if any of the listeners fail to complete successfully
   * @throws T the passed exception if any listeners fail
   */
  public static <T extends Exception> void triggerAndWait(Event event, T exceptionToThrow) throws T {
    ListenerStatus status = trigger(event);
    while (!status.isFinished()){}
    if (status.getCollectiveStatus().equals(Status.ERRORED)) {
      throw exceptionToThrow;
    }
  }

  /**
   * The events framework may be configured to track event creation
   * stack traces (default is off/false).  This method returns whether
   * this capability has been turned on.  If true, then Event objects
   * will fetch and retain the call stack when they are created.
   * 
   * @return true if events framework has been configured to track
   * event creation stack traces, else false
   */
  public static boolean isTrackEventCreationStackTraces() {
    return EVENTS.getConfig().isTrackEventCreationStackTraces();
  }

  private static void checkInit() {
    try {
      EVENTS_LOCK.readLock().lock();
      if (EVENTS == null)
        throw new RuntimeException("Events not initialized, or initialized but shut down.");
    }
    finally {
      EVENTS_LOCK.readLock().unlock();
    }
  }

  /*%%%%%%%%%%%%%% INSTANCE MEMBERS %%%%%%%%%%%%%%*/

  private final EventsConfig _config;
  private final ExecutorService _execService;

  private boolean _isShutDown = false;

  private Map<String, List<EventListener>> _eventCodeMap = new HashMap<>();
  private Map<String, List<EventListener>> _eventTypeMap = new HashMap<>();

  private ReadWriteLock _eventCodeMapLock = new ReentrantReadWriteLock();
  private ReadWriteLock _eventTypeMapLock = new ReentrantReadWriteLock();

  private Events(EventsConfig config) {
    _config = config;
    _execService = Executors.newFixedThreadPool(config.getThreadPoolSize());
  }

  private EventsConfig getConfig() {
    return _config;
  }

  private void addListener(String eventCode, EventListener listener) {
    try {
      _eventCodeMapLock.writeLock().lock();
      checkNotShutDown();
      List<EventListener> listeners = _eventCodeMap.get(eventCode);
      if (listeners == null) {
        listeners = new ArrayList<>();
        _eventCodeMap.put(eventCode, listeners);
      }
      listeners.add(listener);
    }
    finally {
      _eventCodeMapLock.writeLock().unlock();
    }
  }

  private void addListener(Class<? extends Event> eventType, EventListener listener) {
    try {
      _eventTypeMapLock.writeLock().lock();
      checkNotShutDown();
      List<EventListener> listeners = _eventTypeMap.get(eventType.getName());
      if (listeners == null) {
        listeners = new ArrayList<>();
        _eventTypeMap.put(eventType.getName(), listeners);
      }
      listeners.add(listener);
    }
    finally {
      _eventTypeMapLock.writeLock().unlock();
    }
  }

  private void removeListener(EventListener listener) {
    try {
      _eventTypeMapLock.writeLock().lock();
      _eventCodeMapLock.writeLock().lock();
      checkNotShutDown();
      for (List<EventListener> list : _eventTypeMap.values()) {
        list.remove(listener);
      }
      for (List<EventListener> list : _eventCodeMap.values()) {
        list.remove(listener);
      }
    }
    finally {
      _eventCodeMapLock.writeLock().unlock();
      _eventTypeMapLock.writeLock().unlock();
      
    }
  }

  private static class NotificationWrapper implements Callable<String> {

    private EventListener _listener;
    private Event _event;
    private ListenerStatus _statuser;
    
    public NotificationWrapper(EventListener listener, Event event, ListenerStatus statuser) {
      _listener = listener;
      _event = event;
      _statuser = statuser;
    }

    @Override
    public String call() throws Exception {
      try {
        _listener.eventTriggered(_event);
        _statuser.notifySuccess(_listener);
        return ListenerStatus.Status.SUCCESS.toString();
      }
      catch (Exception e) {
        Events.trigger(new ListenerExceptionEvent(_listener, _event, e));
        _statuser.notifyError(_listener);
        return ListenerStatus.Status.ERRORED.toString();
      }
    }
    
  }

  private ListenerStatus submitEvent(Event event) {
    // get list of listeners, notify them each in a different thread
    try {
      _eventCodeMapLock.readLock().lock();
      _eventTypeMapLock.readLock().lock();
      checkNotShutDown();

      // use a set to ensure each listener is notified only once per event
      // accumulate all the listeners we should notify of this event; this includes:
      Set<EventListener> listeners = new HashSet<>();

      //   1. Listeners that subscribed to this event's code
      if (_eventCodeMap.containsKey(event.getEventCode())) {
        listeners.addAll(_eventCodeMap.get(event.getEventCode()));
      }

      //   2. Listeners that subscribed to this event's class
      //   3. Listeners that subscribed to classes that are superclasses of this event's class
      addListenersForClassAndParents(listeners, event.getClass(), _eventTypeMap);

      ListenerStatus statuser = new ListenerStatus(listeners);
      for (EventListener listener : listeners) {
        _execService.submit(new NotificationWrapper(listener, event, statuser));
      }

      return statuser;
    }
    finally {
      _eventTypeMapLock.readLock().unlock();
      _eventCodeMapLock.readLock().unlock();
    }
  }

  private void checkNotShutDown() {
    if (_isShutDown)
      throw new RuntimeException("Events object has been shut down and can no longer process events.");
  }

  private static void addListenersForClassAndParents(Set<EventListener> listeners,
      Class<?> type, Map<String, List<EventListener>> eventTypeMap) {
    if (type == null || type == Object.class) return;
    LOG.debug("Adding listeners for type: " + type);
    // otherwise assume this is an Event type
    if (eventTypeMap.containsKey(type.getName())) {
      listeners.addAll(eventTypeMap.get(type.getName()));
    }
    addListenersForClassAndParents(listeners, type.getSuperclass(), eventTypeMap);
  }

  private void stop() {
    try {
      _eventTypeMapLock.writeLock().lock();
      _eventCodeMapLock.writeLock().lock();
      if (_isShutDown) return;
      _eventCodeMap.clear();
      _eventTypeMap.clear();
      _execService.shutdownNow();
      _isShutDown = true;
    }
    finally {
      _eventCodeMapLock.writeLock().unlock();
      _eventTypeMapLock.writeLock().unlock();
    }
  }
}
