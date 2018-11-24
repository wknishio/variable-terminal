package org.vate.client.connection;

import org.vate.client.VTClient;
import org.vate.client.session.VTClientSessionHandler;
import org.vate.console.VTConsole;

public class VTClientConnectionHandler implements Runnable
{
	private VTClient client;
	private VTClientConnection connection;
	private VTClientSessionHandler sessionHandler;
	// private VTClientAuthenticator authenticator;
	// private VTClientSession session;
	
	public VTClientConnectionHandler(VTClient client, VTClientConnection connection)
	{
		this.client = client;
		this.connection = connection;
		this.sessionHandler = new VTClientSessionHandler(client, connection);
		// this.session = new VTClientSession(client, connection);
		// this.authenticator = new VTClientAuthenticator(session);
	}
	
	public VTClientConnection getConnection()
	{
		return connection;
	}
	
	public VTClientSessionHandler getHandler()
	{
		return sessionHandler;
	}
	
	public void run()
	{
		try
		{
			sessionHandler.getAuthenticator().startTimeoutThread();
			connection.setNonceStreams();
			connection.exchangeNonces(false);
			connection.setAuthenticationStreams();
			VTConsole.print("\nVT>Verifying connection with server...");
			if (connection.verifyConnection())
			{
				VTConsole.print("\nVT>Connection with server verified!");
				// connection.setMultiplexedStreams();
				// connection.startConnection();
				sessionHandler.run();
			}
			else
			{
				VTConsole.print("\nVT>Connection with server invalid!");
				// connection.setSkipLine(true);
				connection.closeConnection();
			}
		}
		catch (Throwable e)
		{
			// VTTerminal.print(e.toString());
			//e.printStackTrace();
			VTConsole.print("\nVT>Connection with server failed!");
			// connection.setSkipLine(true);
			connection.closeConnection();
		}
		sessionHandler.getAuthenticator().stopTimeoutThread();
		System.runFinalization();
		System.gc();
		if (client.getInputMenuBar() != null)
		{
			client.getInputMenuBar().setEnabled(false);
		}
	}
}