package org.gusdb.fgputil;

/**
 * Provides simple timer class for measuring duration of clock time between events.  Also provides
 * human-readable toString of time elapsed.
 * 
 * @author rdoherty
 */
public class Timer {

  private long _startTime;

  public static Timer start() {
    return new Timer();
  }

  public Timer() {
    restart();
  }

  public void restart() {
    _startTime = System.currentTimeMillis();
  }

  public long getElapsed() {
    return System.currentTimeMillis() - _startTime;
  }

  public long getElapsedAndRestart() {
    long now = System.currentTimeMillis();
    long previousInterval = now - _startTime;
    _startTime = now;
    return previousInterval;
  }

  public String getElapsedAsString() {
    return getDurationAsString(getElapsed());
  }

  public static String getDurationAsString(long totalMillis) {
    long millis = totalMillis % 1000;
    long totalSeconds = totalMillis / 1000;
    if (totalSeconds == 0) return  millis + "ms";
    long seconds = totalSeconds % 60;
    long totalMinutes = totalSeconds / 60;
    if (totalMinutes == 0) return seconds + "." + millis + " seconds";
    long minutes = totalMinutes % 60;
    long hours = totalMinutes / 60;
    if (hours == 0) return minutes + ":" + pad10(seconds) + "." + millis;
    return hours + ":" + pad10(minutes) + ":" + pad10(seconds) + "." + millis;
  }

  private static String pad10(long minutes) {
    if (minutes < 10) return "0" + minutes;
    return String.valueOf(minutes);
  }

  @Override
  public String toString() {
    return getElapsedAsString();
  }
}
