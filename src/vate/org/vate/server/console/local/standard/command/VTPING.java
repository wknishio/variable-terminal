package org.vate.server.console.local.standard.command;

import java.net.InetAddress;
import java.util.List;

import org.vate.console.VTConsole;
import org.vate.server.connection.VTServerConnectionHandler;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;
import org.vate.server.session.VTServerSession;

public class VTPING extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTPING()
  {
    this.setFullName("*VTPING");
    this.setAbbreviatedName("*VTPG");
    this.setFullSyntax("*VTPING");
    this.setAbbreviatedSyntax("*VTPG");
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
        message.append("\rVT>List of current client connection latencies on server:\nVT>");
        for (VTServerConnectionHandler handler : connections)
        {
          message.append("\nVT>Number: [" + i++ + "]");
          // message.append("\nVT>Authenticated: [" +
          // (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
          // message.append("\nVT>Login: [" + (handler.getSessionHandler().getLogin() !=
          // null ? handler.getSessionHandler().getLogin() : "") + "]");
          InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();

          if (address != null)
          {
            long millisseconds = 0;
            long nanosseconds = 0;
            String hostAddress = "";
            try
            {
              hostAddress = address.getHostAddress();
              VTServerSession session = handler.getSessionHandler().getSession();
              long clientTime = session.getLocalNanoDelay();
              long serverTime = session.getRemoteNanoDelay();
              nanosseconds = ((clientTime + serverTime) / 2);
              millisseconds = ((clientTime + serverTime) / 2) / 1000000;
            }
            catch (Throwable t)
            {

            }

            message.append("\nVT>Host address: [" + hostAddress + "]\nVT>Estimated connection latency: [" + millisseconds + "] ms or [" + nanosseconds +
            // "]\nVT>Host name: [" +
            // address.getHostName() +
            // "]\nVT>Canonical host name: [" +
            // address.getCanonicalHostName() +
              "] ns\nVT>");
          }
        }
        message.append("\nVT>End of current client connection latencies list\nVT>");
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
