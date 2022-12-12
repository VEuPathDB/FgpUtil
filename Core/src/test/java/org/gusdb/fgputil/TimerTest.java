package org.gusdb.fgputil;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;

public class TimerTest {

  @Test
  public void startGetElapsed() {
    final long start = 0L;
    final long end = 200L;

    Clock clock = Mockito.mock(Clock.class);
    Mockito.when(clock.millis())
        .thenReturn(start)
        .thenReturn(end);
    Timer timer = new Timer(clock);
    Assert.assertEquals(200L, timer.getElapsed());
  }

  @Test
  public void restartAndGetElapsed() {
    final long start = 0L;
    final long restartTime = 200L;
    final long end = 500L;

    Clock clock = Mockito.mock(Clock.class);
    Mockito.when(clock.millis())
        .thenReturn(start)
        .thenReturn(restartTime)
        .thenReturn(end);
    Timer timer = new Timer(clock);
    // 200 fake millis pass
    timer.restart();
    // 300 fake millis pass
    Assert.assertEquals(300L, timer.getElapsed());
  }

  @Test
  public void pauseAndResume() {
    final long start = 0L;
    final long pauseTime = 200L;
    final long resumeTime = 1000L;
    final long endTime = 1200L;

    Clock clock = Mockito.mock(Clock.class);
    Mockito.when(clock.millis())
        .thenReturn(start)
        .thenReturn(pauseTime)
        .thenReturn(resumeTime)
        .thenReturn(endTime);
    Timer timer = new Timer(clock);
    // 200 fake millis pass
    timer.pause();
    // 800 fake millis pass
    timer.resume();
    // 200 fake millis pass

    // Expect 200 + 200 = 400 elapsed
    Assert.assertEquals(400L, timer.getElapsed());
  }

  @Test
  public void getElapsedString() {
    final long start = 0L;
    final long end = 200L;

    Clock clock = Mockito.mock(Clock.class);
    Mockito.when(clock.millis())
        .thenReturn(start)
        .thenReturn(end);
    Timer timer = new Timer(clock);
    Assert.assertEquals("200ms", timer.getElapsedString());
  }
}