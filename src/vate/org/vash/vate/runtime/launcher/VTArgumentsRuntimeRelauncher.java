package org.vash.vate.runtime.launcher;

import org.vash.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTArgumentsRuntimeRelauncher
{
  public static void main(String[] args) throws Exception
  {
    VTStandardConsoleInterruptibleInputStreamByte stream = new VTStandardConsoleInterruptibleInputStreamByte(null);
    try
    {
      while (true)
      {
        try
        {
          Thread.sleep(1000);
          VTMainNativeUtils.executeRuntime(stream, System.out, System.err, args);
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    finally
    {
      VTRuntimeExit.exit(0);
    }
  }
}