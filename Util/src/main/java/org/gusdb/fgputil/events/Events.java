package org.gusdb.fgputil.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.gusdb.fgputil.AlreadyInitializedException;

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

  public static void subscribe(String eventCode, EventListener listener) {
    checkInit();
    EVENTS.addListener(eventCode, listener);
  }

  public static CompletionStatus submit(Event event) {
    checkInit();
    return EVENTS.submitEvent(event);
  }

  private static void checkInit() {
    if (EVENTS == null) throw new RuntimeException("Events not initialized, or initialized but shut down.");
  }

  /*%%%%%%%%%%%%%% INSTANCE MEMBERS %%%%%%%%%%%%%%*/

  private ExecutorService _execService;

  private ReadWriteLock _mapLock = new ReentrantReadWriteLock();

  private Map<String, List<EventListener>> _listenerMap = new HashMap<>();

  private Events(EventsConfig config) {
    _execService = Executors.newFixedThreadPool(config.getThreadPoolSize());
  }

  private void addListener(String eventCode, EventListener listener) {
    try {
      _mapLock.writeLock().lock();
      List<EventListener> listeners = _listenerMap.get(eventCode);
      if (listeners == null) {
        listeners = new ArrayList<>();
        _listenerMap.put(eventCode, listeners);
      }
      listeners.add(listener);
    }
    finally {
      _mapLock.writeLock().unlock();
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
      _mapLock.readLock().lock();
      List<EventListener> listeners = _listenerMap.get(event.getEventCode());
      if (listeners == null) { 
        // no listeners; use empty list
        listeners = new ArrayList<>();
      }
      CompletionStatus statuser = new CompletionStatus(listeners);
      for (EventListener listener : listeners) {
        _execService.submit(new NotificationWrapper(listener, event, statuser));
      }
      return statuser;
    }
    finally {
      _mapLock.readLock().unlock();
    }
  }

  private void stop() {
    _execService.shutdownNow();
  }
}
