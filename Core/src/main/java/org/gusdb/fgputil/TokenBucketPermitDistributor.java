package org.gusdb.fgputil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Distributes permits at a configurable rate. The {@code TokenBucketPermitDistributor} is defined by the rate at which
 * permits are filled, and the maximum number of permits that can be held (burst size). Note that this is
 * just a permit distributor as opposed to a limiter, as it can only tell you if permits are available. It will not
 * do any sleeping or throttling on its own.
 *
 * This is an implementation of the TokenBucket algorithm. See https://en.wikipedia.org/wiki/Token_bucket.
 */
public class TokenBucketPermitDistributor {
  private final double _burstSize;
  private final double _permitFillRatePerNs;
  private final Clock _clock;

  private double _storedPermits;
  private Instant _lastPermitUpdate;

  TokenBucketPermitDistributor(double permitFillRatePerSecond, double burstSize, Clock clock) {
    if (permitFillRatePerSecond <= 0.0) {
      throw new IllegalArgumentException("Sustained rate must be set to a value greater than 0.0.");
    }
    if (burstSize <= 0.0) {
      throw new IllegalArgumentException("Burst rate must be set to a value greater than 0.0.");
    }
    _burstSize = burstSize;
    _clock = clock;
    _permitFillRatePerNs = permitFillRatePerSecond / TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
    _storedPermits = burstSize;
    _lastPermitUpdate = clock.instant();
  }

  /**
   * Constructs a new instance of a rate limiter.
   *
   * @param sustainedRate Number of new permits made available per second.
   * @param burstSize Amount of permit acquisitions that can be obtained in a burst. Effectively, the maximum number
   *                  of permits that can be stored.
   */
  public TokenBucketPermitDistributor(double sustainedRate, double burstSize) {
    this(sustainedRate, burstSize, Clock.systemUTC());
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
    fillPermits();
    if (permits <= _storedPermits) {
      _storedPermits -= permits;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Sets the number of stored permits by computing how many have accumulated since the last update of permits.
   * The number of stored permits will never be filled past the burst size.
   */
  private void fillPermits() {
    final Instant fillInstant = _clock.instant();
    final Duration timeSinceLastPermitUpdate = Duration.between(_lastPermitUpdate, fillInstant);
    final long nanosSinceUpdate = timeSinceLastPermitUpdate.toNanos();
    double newPermits =  nanosSinceUpdate * _permitFillRatePerNs;
    _storedPermits = Math.min(_storedPermits + newPermits, _burstSize);
    _lastPermitUpdate = fillInstant;
  }
}
