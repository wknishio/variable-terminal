package org.vash.vate.server.connection;

import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.session.VTServerSessionHandler;
import org.vash.vate.server.session.VTServerSessionListener;

public class VTServerConnectionHandler implements Runnable
{
  // private VTServer server;
  private VTServerConnector connector;
  private VTServerConnection connection;
  // private VTServerAuthenticator authenticator;
  private VTServerSessionHandler handler;
  // private List<VTServerSessionListener> listeners;
  // private VTServerSession session;
  
  public VTServerConnectionHandler(VTServer server, VTServerConnector connector, VTServerConnection connection)
  {
    // this.server = server;
    this.connector = connector;
    this.connection = connection;
    this.handler = new VTServerSessionHandler(server, connection);
    this.connector.registerConnectionHandler(this);
    // this.session = new VTServerSession(server, connection);
    // this.authenticator = new VTServerAuthenticator(server, connection);
  }
  
  public VTServerConnection getConnection()
  {
    return connection;
  }
  
  public VTServerSessionHandler getSessionHandler()
  {
    return handler;
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      handler.getAuthenticator().startTimeoutThread();
      //VTConsole.print("\rVT>Verifying connection with client...\nVT>");
      if (connection.verifyConnection())
      {
        VTMainConsole.print("\rVT>Connection with client validated!\nVT>");
        // connection.setMultiplexedStreams();
        // connection.startConnection();
        handler.run();
      }
      else
      {
        VTMainConsole.print("\rVT>Connection with client invalidated!\nVT>");
        connection.closeConnection();
      }
    }
    catch (Throwable e)
    {
      // VTTerminal.print(e.toString());
      // e.printStackTrace();
      VTMainConsole.print("\rVT>Connection with client failed!\nVT>");
      connection.closeConnection();
    }
    handler.getAuthenticator().stopTimeoutThread();
    System.runFinalization();
    System.gc();
    /*
     * catch (InterruptedException e) { }
     */
    connector.unregisterConnectionHandler(this);
    try
    {
      // Thread.sleep(1000);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void setSessionListeners(Collection<VTServerSessionListener> listeners)
  {
    handler.setSessionListeners(listeners);
  }
}