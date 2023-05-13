package org.vash.vate.stream.pipe;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.VT;

public final class VTStreamRedirector implements Runnable
{
  private static final int redirectorBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private volatile boolean stopped;
  // private int available = 0;
  private int readed = 0;
  private final byte[] redirectorBuffer = new byte[redirectorBufferSize];
  private final InputStream source;
  private final OutputStream destination;
  private final Closeable notify;
  // private VTTunnelSession session;
  
  public VTStreamRedirector(InputStream source, OutputStream destination)
  {
    this.source = source;
    this.destination = destination;
    //this.destination = new VTBufferedOutputStream(destination, VT.VT_STANDARD_DATA_BUFFER_SIZE, true);
    this.notify = null;
  }
  
  public VTStreamRedirector(InputStream source, OutputStream destination, Closeable notify)
  {
    this.source = source;
    this.destination = destination;
    //this.destination = new VTBufferedOutputStream(destination, VT.VT_STANDARD_DATA_BUFFER_SIZE, true);
    this.notify = notify;
  }
  
  public final void run()
  {
    while (!stopped)
    {
      try
      {
        readed = source.read(redirectorBuffer, 0, redirectorBufferSize);
        if (readed > 0)
        {
          destination.write(redirectorBuffer, 0, readed);
          destination.flush();
        }
        else
        {
          stopped = true;
          break;
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        stopped = true;
        break;
      }
    }
    
    if (notify != null)
    {
      try
      {
        notify.close();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
  }
}