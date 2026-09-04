package org.vash.vate.server.connection;

import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.session.VTServerSessionHandler;
import org.vash.vate.server.session.VTServerSessionListener;

public class VTServerConnectionHandler implements Runnable
{
  private VTServerConnector connector;
  private VTServerConnection connection;
  private VTServerSessionHandler handler;
  private Collection<VTServerConnectionListener> connectionListeners;
  
  public VTServerConnectionHandler(VTServer server, VTServerConnector connector, VTServerConnection connection)
  {
    this.connector = connector;
    this.connection = connection;
    this.handler = new VTServerSessionHandler(server, connection);
    this.connector.registerConnectionHandler(this);
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
      for (VTServerConnectionListener listener : connectionListeners)
      {
        try
        {
          listener.connectionStarted(connection);
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      handler.getAuthenticator().startTimeoutThread();
      connection.verifyConnection();
      handler.run();
    }
    catch (Throwable e)
    {
      VTMainConsole.print("\rVT>Session with client rejected!\nVT>");
      connection.closeConnection();
    }
    handler.getAuthenticator().stopTimeoutThread();
    try
    {
      for (VTServerConnectionListener listener : connectionListeners)
      {
        try
        {
          listener.connectionFinished(connection);
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    connector.unregisterConnectionHandler(this);
    System.gc();
  }
  
  public void setSessionListeners(Collection<VTServerSessionListener> listeners)
  {
    handler.setSessionListeners(listeners);
  }
  
  public void setConnectionListeners(Collection<VTServerConnectionListener> listeners)
  {
    this.connectionListeners = listeners;
  }
}