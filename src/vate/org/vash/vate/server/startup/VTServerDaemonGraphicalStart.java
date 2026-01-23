package org.vash.vate.server.startup;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerDaemonGraphicalStart
{
  public static final void main(String[] args)
  {
    VTMainConsole.setGraphical(false);
    VTMainConsole.setSeparated(false);
    VTMainConsole.setRemoteIcon(true);
    VTMainConsole.setDaemon(true);
    
    if (args.length >= 1)
    {
      VTServer server = new VTServer();
      try
      {
        server.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      server.setDaemon(true);
      server.start();
    }
    else
    {
      VTServer server = new VTServer();
      server.setDaemon(true);
      server.start();
    }
  }
}