package org.vate.server.session;

import org.vate.console.VTConsole;
import org.vate.server.VTServer;
import org.vate.server.authentication.VTServerAuthenticator;
import org.vate.server.connection.VTServerConnection;

public class VTServerSessionHandler implements Runnable
{
	private volatile boolean authenticated;
	// private VTServer server;
	private VTServerConnection connection;
	private VTServerSession session;
	private VTServerAuthenticator authenticator;
	
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
	
	public String getLogin()
	{
		return session.getLogin();
	}
	
	public VTServerSession getSession()
	{
		return session;
	}
	
	public void run()
	{
		authenticated = false;
		VTConsole.print("\rVT>Authenticating session with client...\nVT>");
		try
		{
			connection.setAuthenticationStreams();
			if (authenticator.tryAuthentication())
			{
				connection.setConnectionStreams(authenticator.getDigestedLogin(), authenticator.getDigestedPassword(), authenticator.getLogin(), authenticator.getPassword());
				if (connection.verifyConnection())
				{
					VTConsole.print("\rVT>Session with client accepted!\nVT>");
					processSession();
				}
				else
				{
					VTConsole.print("\rVT>Session with client rejected!\nVT>");
					connection.closeConnection();
				}
			}
			else
			{
				VTConsole.print("\rVT>Session with client rejected!\nVT>");
				connection.closeConnection();
			}
		}
		catch (Throwable e)
		{
			VTConsole.print("\rVT>Session with client failed!\nVT>");
			// e.printStackTrace();
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
			session.setLogin(authenticator.getLogin());
			session.startShell();
			session.startSession();
			session.startSessionThreads();
			session.waitSession();
			session.stopShell();
			session.tryStopSessionThreads();
			//TODO: try to reestablish session
			connection.closeConnection();
			
			session.waitThreads();
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
		}
	}
}