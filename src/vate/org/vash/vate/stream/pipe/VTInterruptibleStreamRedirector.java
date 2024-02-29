package org.vash.vate.stream.pipe;

import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.VT;

public final class VTInterruptibleStreamRedirector implements Runnable
{
  private static final int redirectorBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private boolean stopped;
  // private int available;
  private int readed;
  private final byte[] redirectorBuffer = new byte[redirectorBufferSize];
  private final InputStream source;
  private final OutputStream destination;
  // private Thread redirectThread;
  // private VTTunnelSession session;
  
  public VTInterruptibleStreamRedirector(InputStream source, OutputStream destination)
  {
    this.source = source;
    this.destination = destination;
  }
  
  public void stop()
  {
    stopped = true;
  }
  
  public final void run()
  {
    // redirectThread = Thread.currentThread();
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
        else if (readed < 0)
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
  }
}