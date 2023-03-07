package org.gusdb.fgputil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Distributes permits at a configurable rate. The {@code BurstRateChecker} is defined by the rate at which
 * permits can be used (sustain), and the maximum number of permits that can be held (burst). Note that this is
 * a rate checker as opposed to a limiter, as it can only tell you if you've exhausted your permits. It will not
 * do any sleeping or throttling on its own.
 */
public class BurstyRateChecker {
  private final double _burstRate;
  private final double _nanosSustainedRate;
  private final Clock _clock;

  private double _storedPermits;
  private Instant _lastPermitUpdate;

  BurstyRateChecker(double sustainedRate, double burstRate, Clock clock) {
    if (sustainedRate <= 0.0) {
      throw new IllegalArgumentException("Sustained rate must be set to a value greater than 0.0.");
    }
    if (burstRate <= 0.0) {
      throw new IllegalArgumentException("Burst rate must be set to a value greater than 0.0.");
    }
    _burstRate = burstRate;
    _clock = clock;
    _nanosSustainedRate = sustainedRate * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
    _storedPermits = burstRate;
    _lastPermitUpdate = clock.instant();
  }

  /**
   * Constructs a new instance of a rate limiter.
   *
   * @param sustainedRate Rate of permit acquisitions that can be sustained indefinitely.
   * @param burstRate Amount of permit acquisitions that can be obtained in a burst. Once these are exhausted, clients
   *                  are limited to the sustained rate
   */
  public BurstyRateChecker(double sustainedRate, double burstRate) {
    this(sustainedRate, burstRate, Clock.systemUTC());
  }

  /**
   * Acquire a permit from the rate limiter.
   *
   * @return true if permits are available and false if not.
   */
  public boolean tryAcquire() {
    return tryAcquire(1);
  }

  public synchronized boolean tryAcquire(int permits) {
    syncPermits();
    if (permits <= _storedPermits) {
      _storedPermits -= permits;
      return true;
    } else {
      return false;
    }
  }

  private void syncPermits() {
    final Duration timeSinceLastPermitUpdate = Duration.between(_lastPermitUpdate, _clock.instant());
    final long nanosSinceUpdate = timeSinceLastPermitUpdate.toNanos();
    double newPermits =  _nanosSustainedRate / nanosSinceUpdate;
    _storedPermits = Math.min(_storedPermits + newPermits, _burstRate);
    _lastPermitUpdate = _clock.instant();
  }
}
