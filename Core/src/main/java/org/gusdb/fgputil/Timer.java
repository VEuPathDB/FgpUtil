package org.gusdb.fgputil;

import java.time.Clock;

/**
 * Provides simple timer class for measuring duration of clock time between events.  Also provides
 * human-readable toString of time elapsed.
 * 
 * @author rdoherty
 */
public class Timer {

  private long _lastStartTime;
  private long _accumulatedDuration;
  private boolean _isRunning;
  private Clock _clock;

  public static Timer start() {
    return new Timer();
  }

  public Timer() {
    this(Clock.systemUTC());
  }

  public Timer(Clock clock) {
    _clock = clock;
    restart();
  }

  public void restart() {
    _accumulatedDuration = 0L;
    _lastStartTime = _clock.millis();
    _isRunning = true;
  }

  public long getElapsed() {
    return (_clock.millis() - _lastStartTime) + _accumulatedDuration;
  }

  public void pause() {
    if (_isRunning) {
      _accumulatedDuration += _clock.millis() - _lastStartTime;
      _isRunning = false;
    }
  }

  public void resume() {
    if (!_isRunning) {
      _isRunning = true;
      _lastStartTime = _clock.millis();
    }
  }

  public long getElapsedAndRestart() {
    long now = _clock.millis();
    long previousInterval = (now - _lastStartTime) + _accumulatedDuration;
    _lastStartTime = now;
    _accumulatedDuration = 0L;
    return previousInterval;
  }

  public String getElapsedString() {
    return getDurationString(getElapsed());
  }

  public String getElapsedStringAndRestart() {
    return getDurationString(getElapsedAndRestart());
  }

  public static String getDurationString(long totalMillis) {
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
    return getElapsedString();
  }
}
