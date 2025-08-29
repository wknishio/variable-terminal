package org.vash.vate.client.connection;

import java.util.Collection;

import org.vash.vate.client.VTClient;
import org.vash.vate.client.session.VTClientSessionHandler;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.console.VTMainConsole;

public class VTClientConnectionHandler implements Runnable
{
  private VTClient client;
  private VTClientConnection connection;
  private VTClientSessionHandler handler;
  // private VTClientAuthenticator authenticator;
  // private VTClientSession session;
  
  public VTClientConnectionHandler(VTClient client, VTClientConnection connection)
  {
    this.client = client;
    this.connection = connection;
    this.handler = new VTClientSessionHandler(client, connection);
    // this.session = new VTClientSession(client, connection);
    // this.authenticator = new VTClientAuthenticator(session);
  }
  
  public VTClientConnection getConnection()
  {
    return connection;
  }
  
  public VTClientSessionHandler getHandler()
  {
    return handler;
  }
  
  public void run()
  {
    try
    {
      handler.getAuthenticator().startTimeoutThread();
      //VTConsole.print("\nVT>Verifying connection with server...");
      if (connection.verifyConnection())
      {
        VTMainConsole.print("\nVT>Connection with server validated!");
        // connection.setMultiplexedStreams();
        // connection.startConnection();
        handler.run();
      }
      else
      {
        VTMainConsole.print("\nVT>Connection with server invalidated!");
        // connection.setSkipLine(true);
        connection.closeConnection();
      }
    }
    catch (Throwable e)
    {
      // VTTerminal.print(e.toString());
      // e.printStackTrace();
      VTMainConsole.print("\nVT>Connection with server failed!");
      // connection.setSkipLine(true);
      connection.closeConnection();
    }
    handler.getAuthenticator().stopTimeoutThread();
    System.runFinalization();
    System.gc();
    client.disableInputMenuBar();
  }
  
  public void setSessionListeners(Collection<VTClientSessionListener> listeners)
  {
    handler.setSessionListeners(listeners);
  }
}