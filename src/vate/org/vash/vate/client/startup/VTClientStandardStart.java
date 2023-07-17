package org.vash.vate.client.startup;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientStandardStart
{
  public static void main(String[] args)
  {
    VTConsole.setLanterna(true);
    VTConsole.setGraphical(false);
    VTConsole.setRemoteIcon(true);
    
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
        VTConsole.initialize();
        VTConsole.clear();
        VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Client - Console");
        VTConsole.print(VTHelpManager.printClientModeParametersHelp());
        VTConsole.print(VTHelpManager.printConnnectionParametersHelp());
        if (VTConsole.isGraphical())
        {
          try
          {
            VTConsole.readLine();
          }
          catch (Throwable e)
          {
            
          }
        }
        VTRuntimeExit.exit(0);
      }
      VTConsole.setDaemon(daemon);
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
      VTConsole.setDaemon(daemon);
      VTClient client = new VTClient();
      // client.initialize();
      // client.configure();
      client.setDaemon(daemon);
      client.start();
    }
  }
}