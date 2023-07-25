package org.vash.vate.runtime;

import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.VT;

public class VTRuntimeProcessInputRedirector implements Runnable
{
  private static final int inputBufferSize = VT.VT_REDUCED_BUFFER_SIZE_BYTES;
  private volatile boolean running;
  private int readBytes;
  private final byte[] inputBuffer = new byte[inputBufferSize];
  private InputStream in;
  private OutputStream out;
  
  public VTRuntimeProcessInputRedirector(InputStream in, OutputStream out)
  {
    this.in = in;
    this.out = out;
    this.running = true;
  }
  
  public void close()
  {
    stop();
  }
  
  public void stop()
  {
    running = false;
    finalize();
  }
  
  public void finalize()
  {
    if (in != null)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {
        
      }
      in = null;
    }
    if (out != null)
    {
      out = null;
    }
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    while (running)
    {
      try
      {
        readBytes = in.read(inputBuffer, 0, inputBufferSize);
        if (readBytes > 0 && running)
        {
          out.write(inputBuffer, 0, readBytes);
          out.flush();
        }
        else
        {
          running = false;
          break;
        }
      }
      catch (Throwable e)
      {
        running = false;
        break;
      }
    }
  }
}