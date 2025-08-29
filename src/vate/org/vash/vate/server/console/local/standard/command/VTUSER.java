package org.vash.vate.server.console.local.standard.command;

import java.net.InetAddress;
import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTUSER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTUSER()
  {
    this.setFullName("*VTUSER");
    this.setAbbreviatedName("*VTUS");
    this.setFullSyntax("*VTUSER");
    this.setAbbreviatedSyntax("*VTUS");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    int i = 0;
    message.setLength(0);
    Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
    if (connections.size() > 0)
    {
      message.append("\rVT>List of client connections with server:\nVT>");
      for (VTServerConnectionHandler handler : connections)
      {
        message.append("\nVT>Number: [" + i++ + "]");
        //message.append("\nVT>Authenticated: [" + (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
        message.append("\nVT>User: [" + (handler.getSessionHandler().getUser() != null ? handler.getSessionHandler().getUser() : "") + "]");
        InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
        if (address != null)
        {
          message.append("\nVT>Host address: [" + address.getHostAddress() + "]\nVT>");
        }
      }
      message.append("\nVT>End of client connections with server list\nVT>");
      VTMainConsole.print(message.toString());
    }
    else
    {
      VTMainConsole.print("\rVT>Not connected with clients!\nVT>");
    }
    VTMainConsole.print("\nVT>");
  }
  
  public void close()
  {
    
  }
}
