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
import org.gusdb.fgputil.AlreadyInitializedException;
import org.gusdb.fgputil.events.CompletionStatus.Status;

public class Events {

  /*%%%%%%%%%%%%%% STATIC MEMBERS %%%%%%%%%%%%%%*/

  private static final Logger LOG = Logger.getLogger(Events.class);

  private static final EventsConfig DEFAULT_EVENTS_CONFIG = new EventsConfig() {
    @Override public int getThreadPoolSize() { return 20; }
    @Override public boolean isTrackEventCallStacks() { return false; }
  };

  // singleton Events object
  private static Events EVENTS;

  public static final void init() {
    init(DEFAULT_EVENTS_CONFIG);
  }

  public static synchronized void init(EventsConfig config) {
    if (EVENTS == null) {
      EVENTS = new Events(config);
    }
    else {
      throw new AlreadyInitializedException("Events already initialized.");
    }
  }

  public static synchronized void shutDown() {
    if (EVENTS != null) {
      EVENTS.stop();
      EVENTS = null;
    }
  }

  public static void subscribe(EventListener listener, String... eventCodes) {
    checkInit();
    for (String eventCode : eventCodes) {
      EVENTS.addListener(eventCode, listener);
    }
  }

  @SafeVarargs
  public static void subscribe(EventListener listener, Class<? extends Event>... eventTypes) {
    checkInit();
    for (Class<? extends Event> eventType : eventTypes) {
      EVENTS.addListener(eventType, listener);
    }
  }

  public static CompletionStatus trigger(Event event) {
    checkInit();
    return EVENTS.submitEvent(event);
  }

  public static <T extends Exception> void triggerAndWait(Event event, T exceptionToThrow) throws T {
    CompletionStatus status = trigger(event);
    while (!status.isFinished()){}
    if (status.getCollectiveStatus().equals(Status.ERRORED)) {
      throw exceptionToThrow;
    }
  }

  public static boolean isTrackEventCallStacks() {
    return EVENTS.getConfig().isTrackEventCallStacks();
  }

  private static void checkInit() {
    if (EVENTS == null) throw new RuntimeException("Events not initialized, or initialized but shut down.");
  }

  /*%%%%%%%%%%%%%% INSTANCE MEMBERS %%%%%%%%%%%%%%*/

  private final EventsConfig _config;
  private final ExecutorService _execService;

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
      List<EventListener> listeners = _eventCodeMap.get(eventType);
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

  private static class NotificationWrapper implements Callable<String> {

    private EventListener _listener;
    private Event _event;
    private CompletionStatus _statuser;
    
    public NotificationWrapper(EventListener listener, Event event, CompletionStatus statuser) {
      _listener = listener;
      _event = event;
      _statuser = statuser;
    }

    @Override
    public String call() throws Exception {
      try {
        _listener.eventTriggered(_event);
        _statuser.notifySuccess(_listener);
        return CompletionStatus.Status.SUCCESS.toString();
      }
      catch (Exception e) {
        Events.trigger(new NotificationErrorEvent(_listener, _event, e));
        _statuser.notifyError(_listener);
        return CompletionStatus.Status.ERRORED.toString();
      }
    }
    
  }

  private CompletionStatus submitEvent(Event event) {
    // get list of listeners, notify them each in a different thread
    try {
      _eventCodeMapLock.readLock().lock();
      _eventTypeMapLock.readLock().lock();

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

      CompletionStatus statuser = new CompletionStatus(listeners);
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
    _eventCodeMap.clear();
    _eventTypeMap.clear();
    _execService.shutdownNow();
  }
}
