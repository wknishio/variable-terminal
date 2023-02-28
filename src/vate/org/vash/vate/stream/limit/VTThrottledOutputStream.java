package org.vash.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import engineering.clientside.throttle.NanoThrottle;

public final class VTThrottledOutputStream extends FilterOutputStream
{
  private final NanoThrottle throttler;
  
  public VTThrottledOutputStream(OutputStream out, double bytesPerSecond)
  {
    super(out);
    this.throttler = new NanoThrottle(bytesPerSecond, (1d / 8d), true);
  }
  
  public final void write(int b) throws IOException
  {
    try
    {
      throttler.acquire(1);
    }
    catch (InterruptedException e)
    {
      
    }
    out.write(b);
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    try
    {
      throttler.acquire(len);
    }
    catch (InterruptedException e)
    {
      
    }
    out.write(b, off, len);
  }
  
  public final void setBytesPerSecond(long bytesPerSecond)
  {
    throttler.setRate(bytesPerSecond);
  }
  
  public final double getBytesPerSecond()
  {
    return throttler.getRate();
  }
  
  public final void wakeAllWaitingThreads()
  {
    throttler.wakeAllWaitingThreads();
  }
  
  public final void close() throws IOException
  {
    throttler.setRate(Long.MAX_VALUE);
    throttler.wakeAllWaitingThreads();
    out.close();
  }
}