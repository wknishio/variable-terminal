package org.vate.stream.pipe;

import java.io.InputStream;
import java.io.OutputStream;

import org.vate.VT;

public final class VTInterruptibleStreamRedirector implements Runnable
{
  private static final int redirectorBufferSize = VT.VT_SMALL_DATA_BUFFER_SIZE;
  private volatile boolean stopped;
  private int available;
  private int readed;
  private final byte[] redirectorBuffer = new byte[redirectorBufferSize];
  private final InputStream source;
  private final OutputStream destination;
  // private VTTunnelSession session;

  public VTInterruptibleStreamRedirector(InputStream source, OutputStream destination)
  {
    this.source = source;
    this.destination = destination;
  }

  public final void run()
  {
    while (!stopped)
    {
      try
      {
        available = source.available();
        if (available > 0)
        {
          available = Math.min(available, redirectorBufferSize);
          readed = source.read(redirectorBuffer, 0, available);
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
        else
        {
          Thread.sleep(1);
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