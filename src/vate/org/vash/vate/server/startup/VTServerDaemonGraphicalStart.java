package org.vash.vate.server.startup;

import org.vash.vate.console.VTConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerDaemonGraphicalStart
{
  public static final void main(String[] args)
  {
    // System.setProperty("java.awt.headless", "true");
    VTConsole.setLanterna(true);
    VTConsole.setGraphical(false);
    VTConsole.setRemoteIcon(true);
    VTConsole.setDaemon(true);
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