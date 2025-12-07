package org.vash.vate.server.console.remote.standard.command;

import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

import vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;

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
    if (parsed.length >= 2)
    {
      Collection<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
      if (connections.size() > 0)
      {
        for (VTServerConnectionHandler connectionHandler : connections)
        {
          if (connectionHandler.getConnection() != connection)
          {
            if (connectionHandler.getSessionHandler().isAuthenticated())
            {
              if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
              {
                try
                {
                  connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + command.substring(CommandLineTokenizerMKII.findParameterStart(command, 1)) + "]\nVT>");
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
      session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + command.substring(CommandLineTokenizerMKII.findParameterStart(command, 1)) + "]");
      VTMainConsole.print("\u0007\rVT>Message from client: [" + command.substring(CommandLineTokenizerMKII.findParameterStart(command, 1)) + "]\nVT>");
      // VTConsole.bell();
      connection.getResultWriter().write("\rVT>Message received by server!\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      Collection<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
      if (connections.size() > 0)
      {
        for (VTServerConnectionHandler connectionHandler : connections)
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
      VTMainConsole.print("\u0007\rVT>Message from client: []\nVT>");
      // VTConsole.bell();
      connection.getResultWriter().write("\rVT>Message received by server!\nVT>");
      connection.getResultWriter().flush();
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
