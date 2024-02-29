package org.vash.vate.runtime;

import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.VT;

public class VTRuntimeProcessInputRedirector implements Runnable
{
  private static final int inputBufferSize = VT.VT_REDUCED_BUFFER_SIZE_BYTES;
  private boolean running;
  private boolean verbose;
  private int readBytes;
  private final byte[] inputBuffer = new byte[inputBufferSize];
  private InputStream in;
  private OutputStream out;
  
  public VTRuntimeProcessInputRedirector(InputStream in, OutputStream out, boolean verbose)
  {
    this.in = in;
    this.out = out;
    this.running = true;
    this.verbose = true;
  }
  
  public void finalize()
  {
    //stop();
  }
  
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
          if (verbose && out != null)
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