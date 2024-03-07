/*******************************************************************************
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package engineering.clientside.throttle;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@inheritDoc}
 */
public final class NanoThrottle {

  private static final double ONE_SECOND_NANOS = 1000000000.0;

  private final long nanoStart;
  private final ReentrantLock lock;
  private final double maxBurstSeconds;
  private double storedPermits;
  private double maxPermits;
  private double stableIntervalNanos;
  private long nextFreeTicketNanos;
  //private Object sleepObject = new Object();

  public NanoThrottle(final double permitsPerSecond, final double maxBurstSeconds, final boolean fair) {
    if (permitsPerSecond <= 0.0 || !isFinite(permitsPerSecond)) {
      throw new IllegalArgumentException("rate must be positive");
    }
    this.maxBurstSeconds = maxBurstSeconds;
    this.nanoStart = System.nanoTime();
    this.nextFreeTicketNanos = 0L;
    doSetRate(permitsPerSecond);
    this.lock = new ReentrantLock(fair);
  }

  private static void checkPermits(final int permits) {
    if (permits <= 0) {
      throw new IllegalArgumentException(String
          .format("Requested permits (%s) must be positive", permits));
    }
  }

  /**
   * Returns the sum of {@code val1} and {@code val2} unless it would overflow or underflow in which
   * case {@code Long.MAX_VALUE} or {@code Long.MIN_VALUE} is returned, respectively.
   */
  private static long saturatedAdd(final long val1, final long val2) {
    final long naiveSum = val1 + val2;
    if ((val1 ^ val2) < 0 || (val1 ^ naiveSum) >= 0) {
      return naiveSum;
    }
    return Long.MAX_VALUE + ((naiveSum >>> (Long.SIZE - 1)) ^ 1);
  }

  private void doSetRate(final double permitsPerSecond) {
    reSync(System.nanoTime() - nanoStart);
    this.stableIntervalNanos = ONE_SECOND_NANOS / permitsPerSecond;
    doSetRate(permitsPerSecond, stableIntervalNanos);
  }

  /**
   * {@inheritDoc}
   */
  
  public final double getRate() {
    //lock.lock();
    try {
      return ONE_SECOND_NANOS / stableIntervalNanos;
    } finally {
      //lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  
  public final void setRate(final double permitsPerSecond) {
    if (permitsPerSecond <= 0.0 || !isFinite(permitsPerSecond)) {
      throw new IllegalArgumentException("rate must be positive");
    }
    lock.lock();
    try {
      doSetRate(permitsPerSecond);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  
  public final double acquire(final int permits, final Object sleepObject) throws InterruptedException {
    final long waitDuration = acquireDelayDuration(permits);
    if (waitDuration > 0)
    {
      synchronized (sleepObject)
      {
	    try
	    {
	      NANOSECONDS.timedWait(sleepObject, waitDuration);
	    }
	    catch (Throwable t)
	    {
	      
	    }
      }
    }
    return waitDuration / ONE_SECOND_NANOS;
  }

  /**
   * {@inheritDoc}
   */
  
  public final long acquireDelayDuration(final int permits) {
    checkPermits(permits);
    long elapsedNanos;
    long momentAvailable;
    lock.lock();
    try {
      elapsedNanos = System.nanoTime() - nanoStart;
      momentAvailable = reserveEarliestAvailable(permits, elapsedNanos);
    } finally {
      lock.unlock();
    }
    return max(momentAvailable - elapsedNanos, 0);
  }

  /**
   * {@inheritDoc}
   */
  
  public final boolean tryAcquire(final int permits, final long timeout, final TimeUnit unit, final Object sleepObject)
      throws InterruptedException {
    final long waitDuration = tryAcquireDelayDuration(permits, timeout, unit);
    if (waitDuration < 0) {
      return false;
    }
    if (waitDuration > 0)
    {
      synchronized (sleepObject)
      {
  	    try
  	    {
  	      NANOSECONDS.timedWait(sleepObject, waitDuration);
  	    }
  	    catch (Throwable t)
  	    {
  	      
  	    }
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  
  public final long tryAcquireDelayDuration(final int permits, final long timeout,
      final TimeUnit unit) {
    if (timeout <= 0) {
      return tryAcquire(permits) ? 0 : -1;
    }
    final long waitDuration = unit.toNanos(timeout);
    checkPermits(permits);
    long durationElapsed;
    long momentAvailable;
    lock.lock();
    try {
      durationElapsed = System.nanoTime() - nanoStart;
      if (!canAcquire(durationElapsed, waitDuration)) {
        return -1;
      }
      momentAvailable = reserveEarliestAvailable(permits, durationElapsed);
    } finally {
      lock.unlock();
    }
    return momentAvailable - durationElapsed;
  }

  /**
   * {@inheritDoc}
   */
  
  public final boolean tryAcquire(final int permits) {
    checkPermits(permits);
    lock.lock();
    try {
      final long elapsedNanos = System.nanoTime() - nanoStart;
      if (canAcquire(elapsedNanos, 0)) {
        reserveEarliestAvailable(permits, elapsedNanos);
        return true;
      }
    } finally {
      lock.unlock();
    }
    return false;
  }

  private boolean canAcquire(final long elapsedNanos, final long timeoutNanos) {
    return nextFreeTicketNanos - timeoutNanos <= elapsedNanos;
  }

  /**
   * Reserves the requested number of permits and returns the time that those permits can be used
   * (with one caveat).
   *
   * @return the time that the permits may be used, or, if the permits may be used immediately, an
   * arbitrary past or present time
   */
  private long reserveEarliestAvailable(final int requiredPermits, final long elapsedNanos) {
    reSync(elapsedNanos);
    final long returnValue = nextFreeTicketNanos;
    final double storedPermitsToSpend = min(requiredPermits, this.storedPermits);
    final double freshPermits = requiredPermits - storedPermitsToSpend;
    final long waitNanos = storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend)
        + (long) (freshPermits * stableIntervalNanos);
    this.nextFreeTicketNanos = saturatedAdd(nextFreeTicketNanos, waitNanos);
    this.storedPermits -= storedPermitsToSpend;
    return returnValue;
  }

  /**
   * Updates {@code storedPermits} and {@code nextFreeTicketNanos} based on the current time.
   */
  private void reSync(final long elapsedNanos) {
    if (elapsedNanos > nextFreeTicketNanos) {
      final double newPermits = (elapsedNanos - nextFreeTicketNanos) / coolDownIntervalNanos();
      storedPermits = min(maxPermits, storedPermits + newPermits);
      nextFreeTicketNanos = elapsedNanos;
    }
  }

  
  public String toString() {
    return "Throttle{rate=" + getRate() + '}';
  }
  
  public static boolean isFinite(double d) {
      return Math.abs(d) <= Double.MAX_VALUE;
  }
  
  
  private void doSetRate(double permitsPerSecond, double stableIntervalNanos) {
    final double oldMaxPermits = this.maxPermits;
    maxPermits = maxBurstSeconds * permitsPerSecond;
    storedPermits = oldMaxPermits == 0.0 ? 0.0 : storedPermits * maxPermits / oldMaxPermits;
  }

  /**
   * {@inheritDoc}
   */
  
  private long storedPermitsToWaitTime(final double storedPermits, final double permitsToTake) {
    return 0L;
  }

  /**
   * {@inheritDoc}
   */
  
  private double coolDownIntervalNanos() {
    return stableIntervalNanos;
  }
  
  public void wakeAllWaitingThreads(final Object sleepObject)
  {
  	synchronized (sleepObject)
  	{
  	  sleepObject.notifyAll();
  	}
  }
}
