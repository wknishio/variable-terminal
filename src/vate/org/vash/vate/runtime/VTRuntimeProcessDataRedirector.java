package org.vash.vate.runtime;

import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.VTSystem;

public class VTRuntimeProcessDataRedirector implements Runnable
{
  private static final int inputBufferSize = VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES;
  private volatile boolean running;
  private final boolean close;
  private int readBytes;
  private final byte[] inputBuffer = new byte[inputBufferSize];
  private InputStream in;
  private OutputStream out;
  
  public VTRuntimeProcessDataRedirector(InputStream in, OutputStream out, boolean close)
  {
    this.in = in;
    this.out = out;
    this.close = close;
    this.running = true;
  }
  
//  public void finalize()
//  {
//    stop();
//  }
  
  public void close()
  {
    stop();
  }
  
  public void stop()
  {
    running = false;
    destroy();
  }
  
  public void destroy()
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
      if (close)
      {
        try
        {
          out.close();
        }
        catch (Throwable e)
        {
          
        }
      }
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
          if (out != null)
          {
            try
            {
              out.write(inputBuffer, 0, readBytes);
              out.flush();
            }
            catch (Throwable e)
            {
              out = null;
            }
          }
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