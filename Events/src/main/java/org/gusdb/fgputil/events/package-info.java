/**
 * Provides basic in-memory events framework, including triggering events
 * which can be listened for across the application.  The API can be divided
 * between framework configuration, event triggerers and event listeners.
 * 
 * <ul>
 *   <li>
 *     <strong>Framework Configuration:</strong> The events framework must
 *     be initialized before any events can be triggered or listened to.  Thus,
 *     during application start-up, one of the <code>Events.init</code> methods
 *     should be called.  The no-arg version uses a default configuration, or
 *     a specific configuration can be used.  During application shut-down, the
 *     <code>Events.shutDown</code> method should be called to clean up resources.
 *   </li>
 *   <li>
 *     <strong>Event Listeners:</strong> Code that would like to respond to
 *     events must only implement the <code>EventListener</code> interface and
 *     then subscribe to events of a particular type or that contain a particular
 *     code using <code>Events.subscribe</code>.
 *   </li>
 *   <li>
 *     <strong>Event Triggerers:</strong> Code that would like to trigger events
 *     must instantiate an <code>Event</code> or create and instantiate a subclass
 *     of <code>Event</code> and pass the object to <code>Events.trigger</code> or
 *     <code>Events.triggerAndWait</code>.  <code>Events.trigger</code> will
 *     trigger the event, asynchronously calling the <code>eventTriggered</code>
 *     method of all subscribed listeners.  <code>Events.triggerAndWait</code>
 *     is a convenience method which waits until all listeners have completed
 *     processing before returning.
 *   </li>
 * </ul>
 *
 * @since build-23
 */
package org.gusdb.fgputil.events;
