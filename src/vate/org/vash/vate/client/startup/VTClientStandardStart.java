package org.vash.vate.client.startup;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientStandardStart
{
  public static void main(String[] args)
  {
    VTSystemConsole.setGraphical(false);
    VTSystemConsole.setSeparated(false);
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
        if ("-A".equalsIgnoreCase(args[i]))
        {
          daemon = true;
        }
      }
      if (help && !daemon)
      {
        VTSystemConsole.initialize();
        VTSystemConsole.clear();
        VTSystemConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Client - Console");
        VTSystemConsole.print(VTHelpManager.printClientModeParametersHelp());
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
      VTSystemConsole.setDaemon(daemon);
      VTClient client = new VTClient();
      // client.initialize();
      // client.configure();
      client.setDaemon(daemon);
      client.start();
    }
  }
}