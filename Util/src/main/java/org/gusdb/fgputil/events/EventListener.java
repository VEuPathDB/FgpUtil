package org.gusdb.fgputil.events;

/**
 * Interface for classes that wish to listen for events.
 * 
 * @author ryan
 */
public interface EventListener {

  /**
   * Called with event when events are triggered which this listener
   * is subscribed to.  Note this method will only be called once
   * for a given event.
   * 
   * @param event event triggered
   * @throws Exception if processing of event is unsuccessful
   */
  public void eventTriggered(Event event) throws Exception;

}
