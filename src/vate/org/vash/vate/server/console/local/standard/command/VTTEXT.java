package org.vash.vate.server.console.local.standard.command;

import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

import vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;

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
    if (parsed.length >= 2)
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
                connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + command.substring(CommandLineTokenizerMKII.findParameterStart(command, 1)) + "]\nVT>");
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
        VTMainConsole.print("\rVT>Message sent to clients!\nVT>");
      }
      else
      {
        VTMainConsole.print("\rVT>Not connected with clients!\nVT>");
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
        VTMainConsole.print("\rVT>Message sent to clients!\nVT>");
      }
      else
      {
        VTMainConsole.print("\rVT>Not connected with clients!\nVT>");
      }
    }
  }
  
  public void close()
  {
    
  }
}
