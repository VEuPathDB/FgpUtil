package org.gusdb.fgputil.events;

/**
 * Configuration class for initializing events framework.
 * 
 * @author ryan
 */
public interface EventsConfig {

  /**
   * Returns the desired threadpool size.  Default is 20.  This is
   * the size of the threadpool that will service all event listeners.
   * Every time an event is triggered, each listener for that event
   * is notified in its own thread, so this may need to be a decent
   * size.
   * 
   * @return desired threadpool size
   */
  public int getThreadPoolSize();

  /**
   * Returns whether to track event creation stack traces.  Default
   * is false.  Setting to true could be handy for debug but could
   * also absorb a decent amount of runtime memory.  Turn on cautiously.
   * 
   * @return true if event creation stack traces should be tracked,
   * else false
   */
  public boolean isTrackEventCreationStackTraces();

}
