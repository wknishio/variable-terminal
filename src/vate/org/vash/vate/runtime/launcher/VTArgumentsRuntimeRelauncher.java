package org.vash.vate.runtime.launcher;

import org.vash.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.runtime.VTRuntimeProcessDataRedirector;

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
          Process process = Runtime.getRuntime().exec(args);
          VTRuntimeProcessDataRedirector rin = new VTRuntimeProcessDataRedirector(process.getInputStream(), System.out, false);
          VTRuntimeProcessDataRedirector rerr = new VTRuntimeProcessDataRedirector(process.getErrorStream(), System.err, false);
          VTRuntimeProcessDataRedirector rout = new VTRuntimeProcessDataRedirector(stream, process.getOutputStream(), false);
          Thread tin = new Thread(rin);
          Thread terr = new Thread(rerr);
          Thread tout = new Thread(rout);
          tin.start();
          terr.start();
          tout.start();
          process.waitFor();
          rin.close();
          rerr.close();
          rout.close();
          tin.join();
          terr.join();
          tout.join();
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