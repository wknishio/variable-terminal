package org.vash.vate.server.console.local.standard.command;

import java.net.InetAddress;
import java.util.List;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTUSERS extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTUSERS()
  {
    this.setFullName("*VTUSERS");
    this.setAbbreviatedName("*VTUSR");
    this.setFullSyntax("*VTUSERS");
    this.setAbbreviatedSyntax("*VTUSR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    int i = 0;
    message.setLength(0);
    List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
    synchronized (connections)
    {
      if (connections.size() > 0)
      {
        message.append("\rVT>List of current client connections on server:\nVT>");
        for (VTServerConnectionHandler handler : connections)
        {
          message.append("\nVT>Session Number: [" + i++ + "]");
          message.append("\nVT>Authenticated: [" + (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
          message.append("\nVT>User: [" + (handler.getSessionHandler().getUser() != null ? handler.getSessionHandler().getUser() : "") + "]");
          InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
          if (address != null)
          {
            message.append("\nVT>Host address: [" + address.getHostAddress() + "]\nVT>");
            // "\nVT>Host name: [" + address.getCanonicalHostName() + "]\nVT>");
            // "]\nVT>Host name: [" +
            // address.getHostName() +
            // "]\nVT>Canonical host name: [" +
            // address.getCanonicalHostName() +
            // "]\nVT>");
          }
        }
        message.append("\nVT>End of current client connections list\nVT>");
        VTConsole.print(message.toString());
      }
      else
      {
        VTConsole.print("\rVT>Not connected with clients!\nVT>");
      }
    }
    VTConsole.print("\nVT>");
  }
  
  public void close()
  {
    
  }
}
