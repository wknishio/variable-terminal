package org.vash.vate.runtime.launcher;

import java.io.InputStream;

import org.vash.vate.VT;

public class VTRuntimeLauncherOutputConsumer implements Runnable
{
  private final byte[] buffer = new byte[VT.VT_REDUCED_BUFFER_SIZE_BYTES];
  private InputStream in;
  
  public VTRuntimeLauncherOutputConsumer(InputStream in)
  {
    this.in = in;
  }
  
  public void close()
  {
    try
    {
      in.close();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void run()
  {
    try
    {
      int readed = 1;
      while (readed > 0)
      {
        readed = in.read(buffer, 0, buffer.length);
      }
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      in.close();
    }
    catch (Throwable e)
    {
      
    }
  }
}