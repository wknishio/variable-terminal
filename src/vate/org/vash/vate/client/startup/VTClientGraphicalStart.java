package org.vash.vate.client.startup;

import org.vash.vate.VTSystem;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientGraphicalStart
{
  public static void main(String[] args)
  {
    VTMainConsole.setGraphical(true);
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
        if ("-A".equalsIgnoreCase(args[i]))
        {
          daemon = true;
        }
      }
      if (help && !daemon)
      {
        VTMainConsole.initialize();
        VTMainConsole.setTitle("Variable-Terminal " + VTSystem.VT_VERSION + " - Client - Console");
        VTMainConsole.print(VTHelpManager.printClientModeParametersHelp());
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
      VTClient client = new VTClient();
      try
      {
        client.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      // client.initialize();
      client.setDaemon(daemon);
      client.start();
    }
    else
    {
      VTMainConsole.setDaemon(daemon);
      VTClient client = new VTClient();
      // client.initialize();
      // client.configure();
      client.setDaemon(daemon);
      client.start();
    }
  }
}