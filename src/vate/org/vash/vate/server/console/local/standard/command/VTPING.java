package org.vash.vate.server.console.local.standard.command;

import java.net.InetAddress;
import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;
import org.vash.vate.server.session.VTServerSession;

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
    Collection<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
    if (connections.size() > 0)
    {
      message.append("\rVT>List of client connection latencies with server:\nVT>");
      for (VTServerConnectionHandler handler : connections)
      {
        try
        {
          InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
          if (address != null)
          {
            message.append("\nVT>Number: [" + i++ + "]");
            String hostAddress = address.getHostAddress();
            VTServerSession session = handler.getSessionHandler().getSession();
            long clientTime = session.getRemoteNanoDelay();
            long serverTime = session.getLocalNanoDelay();
            long clientNanoseconds = clientTime;
            long clientMilliseconds = clientTime / 1000000;
            long serverNanoseconds = serverTime;
            long serverMilliseconds = serverTime / 1000000;
            message.append("\nVT>Host address: [" + hostAddress + "]" +
            "\nVT>Client connection latency: [" + clientNanoseconds + "] ns or [" + clientMilliseconds + "] ms" +
            "\nVT>Server connection latency: [" + serverNanoseconds + "] ns or [" + serverMilliseconds + "] ms" +
            "\nVT>");
          }
        }
        catch (Throwable t)
        {
          
        }
      }
      message.append("\nVT>End of client connection latencies with server list\nVT>");
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
