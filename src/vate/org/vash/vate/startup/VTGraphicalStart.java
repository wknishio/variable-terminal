package org.vash.vate.startup;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;

public final class VTGraphicalStart
{
  private static String option;
  
  public static final void main(String[] args)
  {
    VTSystemConsole.setGraphical(true);
    VTSystemConsole.setSeparated(false);
    VTSystemConsole.setRemoteIcon(true);
    VTSystemConsole.setDaemon(false);
    
    VTClient client = new VTClient();
    VTServer server = new VTServer();
    
    if (args.length == 0)
    {
      VTSystemConsole.initialize();
      VTSystemConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
      VTSystemConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Module - (c) " + VT.VT_YEAR + " wknishio@gmail.com\n" + 
      "VT>This software is under MIT license with no warranty, use at your own risk!\n");
      VTSystemConsole.print("VT>Enter module(client as C or server as S, default:C):");
      if (VTSystemConsole.isGraphical())
      {
        VTGraphicalStartDialog dialog = new VTGraphicalStartDialog(VTSystemConsole.getFrame());
        dialog.setVisible(true);
        if (dialog.getMode() == 1)
        {
          try
          {
            // client.setDaemon(true);
            // client.initialize();
            // client.configure();
            client.start();
            return;
          }
          catch (Throwable e)
          {
            //e.printStackTrace();
          }
        }
        else if (dialog.getMode() == 2)
        {
          try
          {
            server.setDaemon(false);
            // server.initialize();
            // server.configure();
            server.start();
            return;
          }
          catch (Throwable e)
          {
            //e.printStackTrace();
          }
        }
      }
      try
      {
        option = VTSystemConsole.readLine(true);
        if (option.toUpperCase().startsWith("S"))
        {
          server.setDaemon(false);
          // server.initialize();
          // server.configure();
          server.start();
        }
        else if (option != null)
        {
          // client.initialize();
          // client.configure();
          client.start();
        }
        else
        {
          VTRuntimeExit.exit(0);
        }
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(0);
      }
    }
    else
    {
      int type = 0;
      for (int i = 0; i < args.length; i++)
      {
        if ("-C".equalsIgnoreCase(args[i]))
        {
          type = 1;
        }
        else if ("-S".equalsIgnoreCase(args[i]))
        {
          type = 2;
        }
        else if ("-D".equalsIgnoreCase(args[i]))
        {
          type = 3;
        }
        else if ("-H".equalsIgnoreCase(args[i]))
        {
          type = 4;
        }
        else if ("-A".equalsIgnoreCase(args[i]))
        {
          type = 5;
        }
        else
        {
          if (client.loadClientSettingsFile(args[i]))
          {
            client.start();
            return;
          }
          if (server.loadServerSettingsFile(args[i]))
          {
            server.start();
            return;
          }
        }
      }
      if (type == 1)
      {
        try
        {
          client.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        // client.initialize();
        client.start();
      }
      else if (type == 2)
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
        server.setDaemon(false);
        server.start();
      }
      else if (type == 3)
      {
        VTSystemConsole.setDaemon(true);
        try
        {
          server.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        // server.initialize();
        server.setDaemon(true);
        server.start();
      }
      else if (type == 4)
      {
        VTSystemConsole.initialize();
        VTSystemConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
        // VTConsole.print(VTHelpManager.printApplicationParametersHelp());
        VTSystemConsole.print(VTHelpManager.printGeneralModeParameterHelp());
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
      else if (type == 5)
      {
        try
        {
          client.parseParameters(args);
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(-1);
        }
        client.setDaemon(true);
        // client.initialize();
        client.start();
      }
      else
      {
        VTSystemConsole.initialize();
        VTSystemConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
        VTSystemConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Module - (c) " + VT.VT_YEAR + " wknishio@gmail.com\n" + 
        "VT>This software is under MIT license with no warranty, use at your own risk!\n");
        VTSystemConsole.print("VT>Enter module(client as C or server as S, default:C):");
        if (VTSystemConsole.isGraphical())
        {
          VTGraphicalStartDialog dialog = new VTGraphicalStartDialog(VTSystemConsole.getFrame());
          dialog.setVisible(true);
          if (dialog.getMode() == 1)
          {
            try
            {
              // client.initialize();
              // client.configure();
              try
              {
                client.parseParameters(args);
              }
              catch (Throwable e)
              {
                
              }
              client.start();
              return;
            }
            catch (Throwable e)
            {
              // e.printStackTrace();
            }
          }
          else if (dialog.getMode() == 2)
          {
            try
            {
              server.setDaemon(false);
              // server.initialize();
              // server.configure();
              try
              {
                server.parseParameters(args);
              }
              catch (Throwable e)
              {
                
              }
              server.start();
              return;
            }
            catch (Throwable e)
            {
              // e.printStackTrace();
            }
          }
        }
        try
        {
          option = VTSystemConsole.readLine(true);
          if (option.toUpperCase().startsWith("S"))
          {
            server.setDaemon(false);
            try
            {
              server.parseParameters(args);
            }
            catch (Throwable e)
            {
              
            }
            // server.initialize();
            // server.configure();
            server.start();
          }
          else
          {
            try
            {
              client.parseParameters(args);
            }
            catch (Throwable e)
            {
              
            }
            // client.initialize();
            // client.configure();
            client.start();
          }
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(0);
        }
      }
    }
  }
}