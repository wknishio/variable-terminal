package org.vash.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import engineering.clientside.throttlevt.NanoThrottle;

public final class VTThrottledOutputStream extends FilterOutputStream
{
  private final NanoThrottle throttler;
  
  public VTThrottledOutputStream(final OutputStream out, final NanoThrottle throttler)
  {
    super(out);
    this.throttler = throttler;
  }
  
  public final void write(final int b) throws IOException
  {
    try
    {
      throttler.acquire(1, this);
    }
    catch (Throwable t)
    {
      
    }
    out.write(b);
  }
  
  public final void write(final byte[] b, final int off, final int len) throws IOException
  {
    try
    {
      throttler.acquire(len, this);
    }
    catch (Throwable t)
    {
      
    }
    out.write(b, off, len);
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
}