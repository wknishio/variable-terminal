package org.vash.vate.runtime;

import java.io.BufferedReader;
import java.io.OutputStream;

public class VTRuntimeProcessTextRedirector implements Runnable
{
  private volatile boolean running;
  private BufferedReader in;
  private OutputStream out;
  
  public VTRuntimeProcessTextRedirector(BufferedReader in, OutputStream out)
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
        String line = in.readLine();
        if (line != null && line.length() > 0 && running)
        {
          byte[] data = line.getBytes("UTF-8");
          out.write(data, 0, data.length);
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