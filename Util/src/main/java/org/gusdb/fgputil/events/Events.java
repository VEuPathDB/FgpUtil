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

import org.gusdb.fgputil.AlreadyInitializedException;
import org.gusdb.fgputil.events.CompletionStatus.Status;

public class Events {

  /*%%%%%%%%%%%%%% STATIC MEMBERS %%%%%%%%%%%%%%*/

  private static final EventsConfig DEFAULT_EVENTS_CONFIG = new EventsConfig() {
    @Override public int getThreadPoolSize() { return 20; }
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

  public static CompletionStatus submit(Event event) {
    checkInit();
    return EVENTS.submitEvent(event);
  }

  public static <T extends Exception> void submitAndWait(Event event, T exceptionToThrow) throws T {
    CompletionStatus status = submit(event);
    while (!status.isFinished()){}
    if (status.getCollectiveStatus().equals(Status.ERRORED)) {
      throw exceptionToThrow;
    }
  }

  private static void checkInit() {
    if (EVENTS == null) throw new RuntimeException("Events not initialized, or initialized but shut down.");
  }

  /*%%%%%%%%%%%%%% INSTANCE MEMBERS %%%%%%%%%%%%%%*/

  private ExecutorService _execService;

  private Map<String, List<EventListener>> _eventCodeMap = new HashMap<>();
  private Map<String, List<EventListener>> _eventTypeMap = new HashMap<>();

  private ReadWriteLock _eventCodeMapLock = new ReentrantReadWriteLock();
  private ReadWriteLock _eventTypeMapLock = new ReentrantReadWriteLock();

  private Events(EventsConfig config) {
    _execService = Executors.newFixedThreadPool(config.getThreadPoolSize());
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
        _listener.notifyEvent(_event);
        _statuser.notifyComplete(_listener);
        return CompletionStatus.Status.COMPLETE.toString();
      }
      catch (Exception e) {
        Events.submit(new NotificationErrorEvent(_listener, _event, e));
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

      List<EventListener> codeListeners = _eventCodeMap.get(event.getEventCode());
      List<EventListener> typeListeners = _eventTypeMap.get(event.getClass().getName());

      // use a set to ensure each listener is notified only once per event
      Set<EventListener> listeners = new HashSet<>();
      if (codeListeners != null) listeners.addAll(codeListeners);
      if (typeListeners != null) listeners.addAll(typeListeners);

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

  private void stop() {
    _eventCodeMap.clear();
    _eventTypeMap.clear();
    _execService.shutdownNow();
  }
}
