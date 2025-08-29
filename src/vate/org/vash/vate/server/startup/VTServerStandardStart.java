package org.vash.vate.server.startup;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public class VTServerStandardStart
{
  public static void main(String[] args)
  {
    VTMainConsole.setGraphical(false);
    VTMainConsole.setSeparated(false);
    VTMainConsole.setRemoteIcon(true);
    VTMainConsole.setDaemon(false);
    
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
        VTMainConsole.initialize();
        VTMainConsole.clear();
        VTMainConsole.setTitle("Variable-Terminal " + VTSystem.VT_VERSION + " - Server - Console");
        VTMainConsole.print(VTHelpManager.printServerModeParametersHelp());
        VTMainConsole.print(VTHelpManager.printConnnectionParametersHelp());
        if (VTMainConsole.isGraphical())
        {
          try
          {
            VTMainConsole.readLine();
          }
          catch (Throwable e)
          {
            
          }
        }
        VTRuntimeExit.exit(0);
      }
      VTMainConsole.setDaemon(daemon);
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
      VTMainConsole.setDaemon(daemon);
      VTServer server = new VTServer();
      server.setDaemon(daemon);
      // server.initialize();
      // server.configure();
      server.start();
    }
  }
}