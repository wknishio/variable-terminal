package org.vate.server.connection;

import org.vate.console.VTConsole;
import org.vate.server.VTServer;
import org.vate.server.session.VTServerSessionHandler;

public class VTServerConnectionHandler implements Runnable
{
	// private VTServer server;
	private VTServerConnector connector;
	private VTServerConnection connection;
	// private VTServerAuthenticator authenticator;
	private VTServerSessionHandler sessionHandler;
	// private VTServerSession session;
	
	public VTServerConnectionHandler(VTServer server, VTServerConnector connector, VTServerConnection connection)
	{
		// this.server = server;
		this.connector = connector;
		this.connection = connection;
		this.sessionHandler = new VTServerSessionHandler(server, connection);
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
		return sessionHandler;
	}
	
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			sessionHandler.getAuthenticator().startTimeoutThread();
			connection.setNonceStreams();
			connection.exchangeNonces(false);
			connection.setAuthenticationStreams();
			VTConsole.print("\rVT>Verifying connection with client...\nVT>");
			if (connection.verifyConnection())
			{
				VTConsole.print("\rVT>Connection with client verified!\nVT>");
				// connection.setMultiplexedStreams();
				// connection.startConnection();
				sessionHandler.run();
			}
			else
			{
				VTConsole.print("\rVT>Connection with client invalid!\nVT>");
				connection.closeConnection();
			}
		}
		catch (Throwable e)
		{
			// VTTerminal.print(e.toString());
			// e.printStackTrace();
			VTConsole.print("\rVT>Connection with client failed!\nVT>");
			connection.closeConnection();
		}
		sessionHandler.getAuthenticator().stopTimeoutThread();
		System.runFinalization();
		System.gc();
		/* catch (InterruptedException e) {
		 * } */
		connector.unregisterConnectionHandler(this);
		try
		{
			// Thread.sleep(1000);
		}
		catch (Throwable t)
		{
			
		}
	}
}