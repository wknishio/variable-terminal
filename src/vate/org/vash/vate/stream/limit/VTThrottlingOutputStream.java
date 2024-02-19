package org.vash.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTThrottlingOutputStream extends FilterOutputStream
{
  private final VTThrottledOutputStream throttled;
  //private final OutputStream unthrottled;
  //private volatile OutputStream current;
  private long bytesPerSecond;
  
  public VTThrottlingOutputStream(OutputStream out)
  {
    super(out);
    this.throttled = new VTThrottledOutputStream(out, 1);
    //this.unthrottled = out;
    //this.current = out;
    this.bytesPerSecond = 0;
    this.setBytesPerSecond(0);
  }
  
  public final void write(int b) throws IOException
  {
    // System.out.println("write:" + current.getClass().getName());
    throttled.write(b);
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    // System.out.println("write:" + current.getClass().getName());
    throttled.write(b, off, len);
  }
  
  public final void setBytesPerSecond(long bytesPerSecond)
  {
    this.bytesPerSecond = bytesPerSecond;
    if (bytesPerSecond > 0)
    {
      throttled.setBytesPerSecond(bytesPerSecond);
      throttled.wakeAllWaitingThreads();
      //current = throttled;
    }
    else
    {
      throttled.setBytesPerSecond(Long.MAX_VALUE);
      throttled.wakeAllWaitingThreads();
      //current = unthrottled;
    }
  }
  
  public final long getBytesPerSecond()
  {
    return bytesPerSecond;
  }
  
  public final void close() throws IOException
  {
    throttled.setBytesPerSecond(Long.MAX_VALUE);
    throttled.wakeAllWaitingThreads();
    //current = unthrottled;
    out.close();
  }
}