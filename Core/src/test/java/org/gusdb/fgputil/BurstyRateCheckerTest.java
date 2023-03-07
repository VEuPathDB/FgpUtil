package org.gusdb.fgputil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@RunWith(MockitoJUnitRunner.class)
public class BurstyRateCheckerTest {
  @Mock
  private Clock clock;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAcquireMoreThanBurstInstantaneously() {
    final Instant now = Instant.now();
    Mockito.when(clock.instant()).thenReturn(now); // Clock is fixed, no time is passing between calls to tryAcquire
    BurstyRateChecker rateLimiter = new BurstyRateChecker(1.0, 5.0, clock);
    // Exhaust our burst, no time is passing between invocations so 6th invocation should fail.
    for (int i = 0; i < 5; i++) {
      Assert.assertTrue(rateLimiter.tryAcquire(1));
    }
    Assert.assertFalse(rateLimiter.tryAcquire(1));
  }

  @Test
  public void testAcquireAtSustainedRate() {
    clock = clockTicksOneSecondPerCall();

    BurstyRateChecker rateLimiter = new BurstyRateChecker(1.0, 5.0, clock);

    // We should be able to acquire a single permit indefinitely, as our clock dictates that we are only calling once per second!
    for (int i = 0; i < 1000; i++) {
      Assert.assertTrue(rateLimiter.tryAcquire(1));
    }
  }

  @Test
  public void testAcquireSlightlyFasterThanRate() {
    clock = clockTicksOneSecondPerCall();
    BurstyRateChecker rateLimiter = new BurstyRateChecker(0.9, 2.0, clock);
    // Each call should leave our bucket with 0.1 fewer tokens (called 1.0/sec, filling at 0.9/sec)
    // After 10 calls, we should find our bucket with insufficient tokens (0.9 < 1.0).
    for (int i = 0; i < 10; i++) {
      Assert.assertTrue(rateLimiter.tryAcquire(1));
    }
    Assert.assertFalse(rateLimiter.tryAcquire(1));
  }

  private Clock clockTicksOneSecondPerCall() {
    return new Clock() {
      final Instant startTime = Instant.now();
      long secondsPassed = 0;

      @Override
      public ZoneId getZone() {
        return ZoneId.systemDefault();
      }

      @Override
      public Clock withZone(ZoneId zone) {
        return null;
      }

      @Override
      public Instant instant() {
        return startTime.plus(Duration.ofSeconds(secondsPassed++));
      }
    };
  }
}