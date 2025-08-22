package org.vash.vate.server.startup;

import org.vash.vate.VT;
import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerGraphicalStart
{
  public static void main(String[] args)
  {
    VTSystemConsole.setLanterna(true);
    VTSystemConsole.setGraphical(true);
    VTSystemConsole.setRemoteIcon(true);
    VTSystemConsole.setDaemon(false);
    
    boolean help = false;
    boolean daemon = false;
    
    if (args.length >= 1)
    {
      for (int i = 0; i < args.length; i++)
      {
        if ("-H".equalsIgnoreCase(args[i]))
        {
          help = true;
        }
        if ("-D".equalsIgnoreCase(args[i]))
        {
          daemon = true;
        }
      }
      if (help && !daemon)
      {
        VTSystemConsole.initialize();
        VTSystemConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Server - Console");
        VTSystemConsole.print(VTHelpManager.printServerModeParametersHelp());
        VTSystemConsole.print(VTHelpManager.printConnnectionParametersHelp());
        if (VTSystemConsole.isGraphical())
        {
          try
          {
            VTSystemConsole.readLine();
          }
          catch (Throwable e)
          {
            
          }
        }
        VTRuntimeExit.exit(0);
      }
      VTSystemConsole.setDaemon(daemon);
      VTServer server = new VTServer();
      try
      {
        server.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      // server.initialize();
      server.setDaemon(daemon);
      server.start();
    }
    else
    {
      VTSystemConsole.setDaemon(daemon);
      VTServer server = new VTServer();
      // server.initialize();
      // server.configure();
      server.setDaemon(daemon);
      server.start();
    }
  }
}