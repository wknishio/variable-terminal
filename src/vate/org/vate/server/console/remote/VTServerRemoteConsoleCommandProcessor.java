package org.vate.server.console.remote;

import java.io.BufferedWriter;

import org.vate.console.command.VTConsoleCommandProcessor;
import org.vate.server.connection.VTServerConnection;
import org.vate.server.session.VTServerSession;

public abstract class VTServerRemoteConsoleCommandProcessor extends VTConsoleCommandProcessor
{
	protected VTServerSession session;
	protected VTServerConnection connection;
	protected BufferedWriter resultWriter;
	protected StringBuilder message = new StringBuilder();

	public VTServerRemoteConsoleCommandProcessor()
	{
		
	}
	
	public void setSession(VTServerSession session)
	{
		this.session = session;
		this.connection = session.getConnection();
		this.resultWriter = session.getConnection().getResultWriter();
	}
}