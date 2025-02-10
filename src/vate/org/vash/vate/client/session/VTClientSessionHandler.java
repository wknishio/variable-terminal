package org.vash.vate.client.session;

import java.util.Collection;

import org.vash.vate.client.VTClient;
import org.vash.vate.client.authentication.VTClientAuthenticator;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.console.VTConsole;
import org.vash.vate.monitor.VTDataMonitorConnection;

public class VTClientSessionHandler implements Runnable
{
  private boolean authenticated;
  // private VTClient client;
  private VTClientConnection connection;
  private VTClientSession session;
  private VTClientAuthenticator authenticator;
  private Collection<VTClientSessionListener> listeners;
  
  public VTClientSessionHandler(VTClient client, VTClientConnection connection)
  {
    this.authenticated = false;
    // this.client = client;
    this.connection = connection;
    this.authenticator = new VTClientAuthenticator(client, connection);
    this.session = new VTClientSession(client, connection);
    // this.session.initialize();
  }
  
  /* public VTClientSession getSession() { return this.session; } */
  
  public VTClientAuthenticator getAuthenticator()
  {
    return authenticator;
  }
  
  public boolean isAuthenticated()
  {
    return authenticated;
  }
  
  public void run()
  {
    authenticated = false;
    //VTConsole.print("\nVT>Authenticating session with server...");
    try
    {
      connection.setAuthenticationStreams();
      if (authenticator.tryAuthentication() && connection.setConnectionStreams(authenticator.getDigestedCredential()))
      {
        VTConsole.print("\nVT>Session with server accepted!");
        processSession();
      }
      else
      {
        // connection.setSkipLine(true);
        VTConsole.print("\nVT>Session with server rejected!");
        connection.closeConnection();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // connection.setSkipLine(true);
      VTConsole.print("\nVT>Session with server failed!");
      connection.closeConnection();
    }
    System.runFinalization();
    System.gc();
  }
  
  private void processSession()
  {
    boolean started = false;
    VTDataMonitorConnection dataConnection = null;
    try
    {
      connection.startConnection();
      session.initialize();
      session.negotiateShell();
      session.startSession();
      session.startSessionThreads();
      authenticated = true;
      started = true;
      try
      {
        for (VTClientSessionListener listener : listeners)
        {
          try
          {
            listener.sessionStarted(session);
          }
          catch (Throwable t)
          {
            
          }
        }
      }
      catch (Throwable t)
      {
        
      }
      dataConnection = new VTDataMonitorConnection(connection.getMultiplexedConnectionInputStream(), connection.getMultiplexedConnectionOutputStream());
      if (session.getClient().getMonitorService() != null)
      {
        session.getClient().getMonitorService().addDataConnection(dataConnection);
      }
      session.waitSession();
      session.tryStopSessionThreads();
      connection.closeConnection();
      session.waitThreads();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    if (started)
    {
      try
      {
        for (VTClientSessionListener listener : listeners)
        {
          try
          {
            listener.sessionFinished(session);
          }
          catch (Throwable t)
          {
            
          }
        }
      }
      catch (Throwable t)
      {
        
      }
    }
    if (dataConnection != null && session.getClient().getMonitorService() != null)
    {
      session.getClient().getMonitorService().removeDataConnection(dataConnection);
    }
    session.clearSessionCloseables();
  }
  
  public void setSessionListeners(Collection<VTClientSessionListener> listeners)
  {
    this.listeners = listeners;
  }
}