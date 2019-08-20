package org.vate.server.console.local;

import org.vate.console.command.VTConsoleCommandProcessor;
import org.vate.server.VTServer;

public abstract class VTServerLocalConsoleCommandProcessor extends VTConsoleCommandProcessor
{
	protected VTServer server;
	protected StringBuilder message = new StringBuilder();

	public VTServerLocalConsoleCommandProcessor()
	{
		
	}
	
	public void setServer(VTServer server)
	{
		this.server = server;
	}
}
