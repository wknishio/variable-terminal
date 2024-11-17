package org.vash.vate.server.console.local.standard.command;

import java.util.Collection;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTTEXT extends VTServerStandardLocalConsoleCommandProcessor
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
        Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + parsed[1] + "]\nVT>");
                  connectionHandler.getConnection().getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  // VTTerminal.print("\rVT>Error
                  // detected when sending
                  // message!\nVT>");
                }
              }
            }
          }
          VTConsole.print("\rVT>Message sent to clients!\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Not connected with clients!\nVT>");
        }
      }
      else if (parsed.length == 1)
      {
        Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: []\nVT>");
                  connectionHandler.getConnection().getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  // VTTerminal.print("\rVT>Error
                  // detected when sending
                  // message!\nVT>");
                }
              }
            }
          }
          VTConsole.print("\rVT>Message sent to clients!\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Not connected with clients!\nVT>");
        }
      }
    }
    else
    {
      if (command.toUpperCase().startsWith("*VTTEXT "))
      {
        // command = StringEscapeUtils.unescapeJava(command);
        Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + command.substring(8) + "]\nVT>");
                  connectionHandler.getConnection().getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  // VTTerminal.print("\rVT>Error
                  // detected when sending
                  // message!\nVT>");
                }
              }
            }
          }
          VTConsole.print("\rVT>Message sent to clients!\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Not connected with clients!\nVT>");
        }
      }
      else if (command.toUpperCase().startsWith("*VTTX "))
      {
        // command = StringEscapeUtils.unescapeJava(command);
        Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + command.substring(6) + "]\nVT>");
                  connectionHandler.getConnection().getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  // VTTerminal.print("\rVT>Error
                  // detected when sending
                  // message!\nVT>");
                }
              }
            }
          }
          VTConsole.print("\rVT>Message sent to clients!\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Not connected with clients!\nVT>");
        }
      }
      else
      {
        Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
        if (connections.size() > 0)
        {
          for (VTServerConnectionHandler connectionHandler : connections)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: []\nVT>");
                  connectionHandler.getConnection().getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  // VTTerminal.print("\rVT>Error
                  // detected when sending
                  // message!\nVT>");
                }
              }
            }
          }
          VTConsole.print("\rVT>Message sent to clients!\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Not connected with clients!\nVT>");
        }
      }
    }
  }
  
  public void close()
  {
    
  }
}
