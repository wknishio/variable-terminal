package org.vash.vate.server.session;

import java.util.Collection;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.monitor.VTDataMonitorConnection;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.authentication.VTServerAuthenticator;
import org.vash.vate.server.connection.VTServerConnection;

public class VTServerSessionHandler implements Runnable
{
  private boolean authenticated;
  // private VTServer server;
  private VTServerConnection connection;
  private VTServerSession session;
  private VTServerAuthenticator authenticator;
  private Collection<VTServerSessionListener> listeners;
  
  public VTServerSessionHandler(VTServer server, VTServerConnection connection)
  {
    this.authenticated = false;
    // this.server = server;
    this.connection = connection;
    this.authenticator = new VTServerAuthenticator(server, connection);
    this.session = new VTServerSession(server, connection);
    // this.session.initialize();
  }
  
  public VTServerAuthenticator getAuthenticator()
  {
    return authenticator;
  }
  
  public boolean isAuthenticated()
  {
    return authenticated;
  }
  
  public String getUser()
  {
    return session.getUser();
  }
  
  public VTServerSession getSession()
  {
    return session;
  }
  
  public void run()
  {
    authenticated = false;
    //VTConsole.print("\rVT>Authenticating session with client...\nVT>");
    try
    {
      connection.setAuthenticationStreams();
      if (authenticator.tryAuthentication() && connection.setConnectionStreams(authenticator.getDigestedCredential()))
      {
        VTMainConsole.print("\rVT>Session with client accepted!\nVT>");
        processSession();
      }
      else
      {
        VTMainConsole.print("\rVT>Session with client rejected!\nVT>");
        connection.closeConnection();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      VTMainConsole.print("\rVT>Session with client failed!\nVT>");
      connection.closeConnection();
    }
//    System.runFinalization();
//    System.gc();
  }
  
  private void processSession()
  {
    boolean started = false;
    VTDataMonitorConnection dataConnection = null;
    try
    {
      connection.startConnection();
      session.initialize();
      session.setUser(authenticator.getUser());
      session.negotiateShell();
      session.startShell();
      session.startSession();
      session.startSessionThreads();
      authenticated = true;
      started = true;
      try
      {
        for (VTServerSessionListener listener : listeners)
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
      if (session.getServer().getMonitorService() != null)
      {
        session.getServer().getMonitorService().addDataConnection(dataConnection);
      }
      session.waitSession();
      session.stopShell();
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
        for (VTServerSessionListener listener : listeners)
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
    if (dataConnection != null && session.getServer().getMonitorService() != null)
    {
      session.getServer().getMonitorService().removeDataConnection(dataConnection);
    }
    session.clearSessionCloseables();
  }
  
  public void setSessionListeners(Collection<VTServerSessionListener> listeners)
  {
    this.listeners = listeners;
  }
}