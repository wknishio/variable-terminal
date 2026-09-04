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
  private Collection<VTClientConnectionListener> connectionListeners;
  
  public VTClientConnectionHandler(VTClient client, VTClientConnection connection)
  {
    this.client = client;
    this.connection = connection;
    this.handler = new VTClientSessionHandler(client, connection);
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
      for (VTClientConnectionListener listener : connectionListeners)
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
      VTMainConsole.print("\nVT>Session with server rejected!");
      connection.closeConnection();
    }
    handler.getAuthenticator().stopTimeoutThread();
    client.disableInputMenuBar();
    try
    {
      for (VTClientConnectionListener listener : connectionListeners)
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
    System.gc();
  }
  
  public void setSessionListeners(Collection<VTClientSessionListener> listeners)
  {
    handler.setSessionListeners(listeners);
  }
  
  public void setConnectionListeners(Collection<VTClientConnectionListener> listeners)
  {
    this.connectionListeners = listeners;
  }
}