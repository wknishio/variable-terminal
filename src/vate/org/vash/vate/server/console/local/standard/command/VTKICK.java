package org.vash.vate.server.console.local.standard.command;

import java.util.List;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTKICK extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTKICK()
  {
    this.setFullName("*VTKICK");
    this.setAbbreviatedName("*VTKC");
    this.setFullSyntax("*VTKICK [SESSION]");
    this.setAbbreviatedSyntax("*VTKC [SN]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
      if (connections.size() > 0)
      {
        //VTConsole.print("\rVT>Disconnecting all clients from server...\nVT>");
        for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
        {
          connectionHandler.getConnection().closeSockets();
        }
        VTConsole.print("\rVT>Disconnected all clients from server!\nVT>");
      }
      else
      {
        VTConsole.print("\rVT>Not connected with clients!\nVT>");
      }
    }
    else if (parsed.length >= 2)
    {
      try
      {
        int number = Integer.parseInt(parsed[1]);
        if (number >= 0)
        {
          List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
          if (connections.size() > 0)
          {
            if (connections.size() >= number)
            {
              //VTConsole.print("\rVT>Disconnecting client of number [" + number + "] from server...\nVT>");
              connections.get(number).getConnection().closeSockets();
              VTConsole.print("\rVT>Disconnected client of number [" + number + "] from server!\nVT>");
            }
            else
            {
              VTConsole.print("\rVT>Client number [" + parsed[1] + "] is not valid!\nVT>");
            }
          }
          else
          {
            VTConsole.print("\rVT>Not connected with clients!\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      catch (NumberFormatException e)
      {
        VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
      }
    }
    else
    {
      VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
    }
  }
  
  public void close()
  {
    
  }
}
