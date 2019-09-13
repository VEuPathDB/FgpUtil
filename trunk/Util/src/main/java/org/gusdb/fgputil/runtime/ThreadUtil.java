package org.gusdb.fgputil.runtime;

public class ThreadUtil {

  /**
   * See Javadoc for Thread.sleep().  However, this function does
   * not throw InterruptedException.  Instead, it returns true if the
   * thread is interrupted during sleep; else returns false.  This
   * enables the common pattern of breaking from a loop with a pause
   * at the end with one line of code if the thread is interrupted
   * during the pause.
   * 
   * @param millis length of time to sleep in milliseconds
   * @return true if thread is interrupted during sleep, else false
   */
  public static boolean sleep(long millis) {
    try {
      Thread.sleep(millis);
      return false;
    }
    catch (InterruptedException ex) {
      return true;
    }
  }
}
