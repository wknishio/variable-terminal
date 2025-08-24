package org.vash.vate.server.startup;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerDaemonStandardStart
{
  public static final void main(String[] args)
  {
    // System.setProperty("java.awt.headless", "true");
    VTSystemConsole.setGraphical(false);
    VTSystemConsole.setSeparated(false);
    VTSystemConsole.setRemoteIcon(true);
    VTSystemConsole.setDaemon(true);
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