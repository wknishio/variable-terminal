package org.vash.vate.runtime.launcher;

import org.vash.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.runtime.VTRuntimeProcessInputRedirector;

public class VTArgumentsRuntimeLauncherDaemon
{
  public static void main(String[] args) throws Exception
  {
    VTStandardConsoleInterruptibleInputStreamByte stream = new VTStandardConsoleInterruptibleInputStreamByte();
    // VTNativeUtils.detachConsole();
    try
    {
      Thread.sleep(1000);
      Process process = Runtime.getRuntime().exec(args);
      VTRuntimeProcessInputRedirector rin = new VTRuntimeProcessInputRedirector(process.getInputStream(), System.out, true);
      VTRuntimeProcessInputRedirector rerr = new VTRuntimeProcessInputRedirector(process.getErrorStream(), System.err, true);
      VTRuntimeProcessInputRedirector rout = new VTRuntimeProcessInputRedirector(stream, process.getOutputStream(), true);
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
    VTRuntimeExit.exit(0);
  }
}