package org.vate.runtime.launcher;

import java.io.InputStream;

import org.vate.VT;

public class VTRuntimeLauncherOutputConsumer implements Runnable
{
  private final byte[] buffer = new byte[VT.VT_SMALL_DATA_BUFFER_SIZE];
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