package org.vash.vate.runtime;

import java.io.BufferedWriter;
import java.io.InputStreamReader;

import org.vash.vate.VT;

public class VTRuntimeProcessOutputConsumer implements Runnable
{
  private static final int resultBufferSize = VT.VT_REDUCED_BUFFER_SIZE_BYTES;
  private boolean verbose;
  private boolean running;
  private int readChars;
  private final char[] resultBuffer = new char[resultBufferSize];
  private InputStreamReader in;
  private BufferedWriter out;
  
  public VTRuntimeProcessOutputConsumer(InputStreamReader in, BufferedWriter out, boolean verbose)
  {
    this.in = in;
    this.out = out;
    this.verbose = verbose;
    this.running = true;
    if (in == null || out == null)
    {
      this.running = false;
    }
  }
  
  public void finalize()
  {
    //stop();
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
    }
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    while (running)
    {
      try
      {
        readChars = in.read(resultBuffer, 0, resultBufferSize);
        if (readChars > 0 && running)
        {
          if (verbose)
          {
            out.write(resultBuffer, 0, readChars);
            out.flush();
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