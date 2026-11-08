package org.vash.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.vash.vate.engineering.clientside.throttle.NanoThrottle;

public final class VTThrottledOutputStream extends FilterOutputStream
{
  private final NanoThrottle throttler;
  private long byteRate;
  
  public VTThrottledOutputStream(final OutputStream out, final NanoThrottle throttler)
  {
    super(out);
    this.throttler = throttler;
  }
  
  public final void write(final int b) throws IOException
  {
    out.write(b);
    try
    {
      throttler.acquire(1, this);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void write(final byte[] b, final int off, final int len) throws IOException
  {
    out.write(b, off, len);
    try
    {
      throttler.acquire(len, this);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void setBytesPerSecond(final long bytesPerSecond)
  {
    throttler.setRate(bytesPerSecond);
  }
  
  public final double getBytesPerSecond()
  {
    return throttler.getRate();
  }
  
  public final void close()
  {
    try
    {
      out.close();
    }
    catch (Throwable t)
    {
      
    }
    throttler.setRate(Long.MAX_VALUE);
    throttler.wakeAllWaitingThreads(this);
  }
  
  public final void bypass()
  {
    byteRate = (long) throttler.getRate();
    throttler.setRate(Long.MAX_VALUE);
    throttler.wakeAllWaitingThreads(this);
  }
  
  public final void restore()
  {
    throttler.setRate(byteRate);
  }
}