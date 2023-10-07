package org.vash.vate.server.console.remote.standard.command;

import java.util.List;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTTEXT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTTEXT()
  {
    this.setFullName("*VTTEXT");
    this.setAbbreviatedName("*VTTX");
    this.setFullSyntax("*VTTEXT <TEXT>");
    this.setAbbreviatedSyntax("*VTTX <TX>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    // System.out.println("command:" + command);
    if (command.contains("\""))
    {
      if (parsed.length >= 2)
      {
        List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
          {
            if (connectionHandler.getConnection() != connection)
            {
              if (connectionHandler.getSessionHandler().isAuthenticated())
              {
                if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
                {
                  try
                  {
                    connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + parsed[1] + "]\nVT>");
                    connectionHandler.getConnection().getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                }
              }
            }
          }
        }
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + parsed[1] + "]");
        VTConsole.print("\u0007\rVT>Message from client: [" + parsed[1] + "]\nVT>");
        // VTConsole.bell();
        connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
        connection.getResultWriter().flush();
      }
      else if (parsed.length == 1)
      {
        List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
          {
            if (connectionHandler.getConnection() != connection)
            {
              if (connectionHandler.getSessionHandler().isAuthenticated())
              {
                if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
                {
                  try
                  {
                    connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: []\nVT>");
                    connectionHandler.getConnection().getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                }
              }
            }
          }
        }
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[]");
        VTConsole.print("\u0007\rVT>Message from client: []\nVT>");
        // VTConsole.bell();
        connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    else
    {
      if (command.toUpperCase().startsWith("*VTTEXT "))
      {
        // command = StringEscapeUtils.unescapeJava(command);
        List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
          {
            if (connectionHandler.getConnection() != connection)
            {
              if (connectionHandler.getSessionHandler().isAuthenticated())
              {
                if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
                {
                  try
                  {
                    connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + command.substring(8) + "]\nVT>");
                    connectionHandler.getConnection().getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                }
              }
            }
          }
        }
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + command.substring(8) + "]");
        VTConsole.print("\u0007\rVT>Message from client: [" + command.substring(8) + "]\nVT>");
        // VTConsole.bell();
        connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
        connection.getResultWriter().flush();
      }
      else if (command.toUpperCase().startsWith("*VTTX "))
      {
        // command = StringEscapeUtils.unescapeJava(command);
        List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
          {
            if (connectionHandler.getConnection() != connection)
            {
              if (connectionHandler.getSessionHandler().isAuthenticated())
              {
                if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
                {
                  try
                  {
                    connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + command.substring(6) + "]\nVT>");
                    connectionHandler.getConnection().getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                }
              }
            }
          }
        }
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + command.substring(6) + "]");
        VTConsole.print("\u0007\rVT>Message from client: [" + command.substring(6) + "]\nVT>");
        // VTConsole.bell();
        connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections.toArray(new VTServerConnectionHandler[] {}))
          {
            if (connectionHandler.getConnection() != connection)
            {
              if (connectionHandler.getSessionHandler().isAuthenticated())
              {
                if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
                {
                  try
                  {
                    connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: []\nVT>");
                    connectionHandler.getConnection().getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                }
              }
            }
          }
        }
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[]");
        VTConsole.print("\u0007\rVT>Message from client: []\nVT>");
        // VTConsole.bell();
        connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
        connection.getResultWriter().flush();
      }
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
