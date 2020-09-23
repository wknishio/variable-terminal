package org.vate.client.console.remote.standard.command;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTEXIT extends VTClientStandardRemoteConsoleCommandProcessor
{
	public VTEXIT()
	{
		this.setFullName("*VTEXIT");
		this.setAbbreviatedName("*VTEX");
		this.setFullSyntax("*VTEXIT");
		this.setAbbreviatedSyntax("*VTEX");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		connection.getCommandWriter().write(command + "\n");
		connection.getCommandWriter().flush();
		VTConsole.print("\nVT>Disconnecting from server...");
		// connection.setSkipLine(true);
		connection.closeSockets();
		return;
	}
	
	public void close()
	{
		
	}
}
