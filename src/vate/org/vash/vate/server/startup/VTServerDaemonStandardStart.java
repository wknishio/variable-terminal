package org.vash.vate.server.startup;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerDaemonStandardStart
{
  public static final void main(String[] args)
  {
    // System.setProperty("java.awt.headless", "true");
    VTMainConsole.setGraphical(false);
    VTMainConsole.setSeparated(false);
    VTMainConsole.setRemoteIcon(true);
    VTMainConsole.setDaemon(true);
    VTServer server = new VTServer();
    server.setDaemon(true);
    
    if (args.length >= 1)
    {
      try
      {
        server.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      // server.initialize();
      server.start();
    }
    else
    {
      // server.initialize();
      server.start();
    }
  }
}