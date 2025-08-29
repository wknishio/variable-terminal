package org.vash.vate.server.console.local.standard.command;

import java.util.Collection;
import java.util.Iterator;

import org.vash.vate.console.VTMainConsole;
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
      Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
      if (connections.size() > 0)
      {
        //VTConsole.print("\rVT>Disconnecting all clients from server...\nVT>");
        for (VTServerConnectionHandler connectionHandler : connections)
        {
          connectionHandler.getConnection().closeSockets();
        }
        VTMainConsole.print("\rVT>Disconnected all clients from server!\nVT>");
      }
      else
      {
        VTMainConsole.print("\rVT>Not connected with clients!\nVT>");
      }
    }
    else if (parsed.length >= 2)
    {
      try
      {
        int number = Integer.parseInt(parsed[1]);
        if (number >= 0)
        {
          Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
          if (connections.size() > 0)
          {
            if (connections.size() >= number)
            {
              //VTConsole.print("\rVT>Disconnecting client of number [" + number + "] from server...\nVT>");
              Iterator<VTServerConnectionHandler> iterator = connections.iterator();
              VTServerConnectionHandler handler = null;
              for (int i = 0; i < number; i++)
              {
                handler = iterator.next();
              }
              handler.getConnection().closeSockets();
              //connections.get(number).getConnection().closeSockets();
              VTMainConsole.print("\rVT>Disconnected client of number [" + number + "] from server!\nVT>");
            }
            else
            {
              VTMainConsole.print("\rVT>Client number [" + parsed[1] + "] is not valid!\nVT>");
            }
          }
          else
          {
            VTMainConsole.print("\rVT>Not connected with clients!\nVT>");
          }
        }
        else
        {
          VTMainConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      catch (NumberFormatException e)
      {
        VTMainConsole.print("\rVT>Client number [" + parsed[1] + "] is not valid!\nVT>");
      }
      catch (Throwable t)
      {
        VTMainConsole.print("\rVT>Client number [" + parsed[1] + "] is not valid!\nVT>");
      }
    }
    else
    {
      VTMainConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
    }
  }
  
  public void close()
  {
    
  }
}
