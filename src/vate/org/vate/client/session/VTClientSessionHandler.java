package org.vate.client.session;

import org.vate.client.VTClient;
import org.vate.client.authentication.VTClientAuthenticator;
import org.vate.client.connection.VTClientConnection;
import org.vate.console.VTConsole;

public class VTClientSessionHandler implements Runnable
{
  private volatile boolean authenticated;
  // private VTClient client;
  private VTClientConnection connection;
  private VTClientSession session;
  private VTClientAuthenticator authenticator;

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
    VTConsole.print("\nVT>Authenticating session with server...");
    try
    {
      connection.setAuthenticationStreams();
      if (authenticator.tryAuthentication())
      {
        connection.setConnectionStreams(authenticator.getDigestedLogin(), authenticator.getDigestedPassword(), authenticator.getLogin(), authenticator.getPassword());
        if (connection.verifyConnection())
        {
          VTConsole.print("\nVT>Session with server accepted!");
          processSession();
        }
        else
        {
          VTConsole.print("\nVT>Session with server rejected!");
          connection.closeConnection();
        }
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
    try
    {
      connection.startConnection();
      session.initialize();
      authenticated = true;
      // connection.startConnection();
      session.startSession();
      session.startSessionThreads();
      // session.getClient().getClientConnector().setConnectedOnce(true);
      session.waitSession();
      session.tryStopSessionThreads();
      // TODO: try to reestablish session
      connection.closeConnection();
      session.waitThreads();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}