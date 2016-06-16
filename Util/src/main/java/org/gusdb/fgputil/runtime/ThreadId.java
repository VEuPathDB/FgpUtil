package org.gusdb.fgputil.runtime;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages integer IDs assigned to threads within the application.  IDs are
 * assigned in incremental order starting with 0[,1,2,3,4...].  A typical
 * lifecycle is for client code to assign() an ID at the beginning of the
 * thread's life (or upon fetching it from a thread pool), get() the ID at
 * various times during processing, then unassign() at the end of the thread's
 * life (or before it is returned to a thread pool). 
 */
public class ThreadId {

  // Atomic integer containing the next thread ID to be assigned
  private static final AtomicInteger nextId = new AtomicInteger(0);

  // Thread local variable containing each thread's ID (null if not assigned)
  private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>();

  /**
   * Assigns a new ID to this thread and returns the ID
   * 
   * @return new ID of this thread
   */
  public static int assign() {
    threadId.set(nextId.getAndIncrement());
    return threadId.get();
  }

  /**
   * @return ID of this thread or null if none has been assigned
   */
  public static Integer get() {
    return threadId.get();
  }

  /**
   * Unassigns the current ID if one is assigned.  Subsequent calls to get()
   * will return null until assign() is called again.
   *
   * @return previous ID of this thread or null if none had been assigned
   */
  public static Integer unassign() {
    int previousValue = threadId.get();
    threadId.remove();
    return previousValue;
  }

}
