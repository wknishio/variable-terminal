package org.vate.client.console.remote.standard.command;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTDISCONNECT extends VTClientStandardRemoteConsoleCommandProcessor
{
	public VTDISCONNECT()
	{
		this.setFullName("*VTDISCONNECT");
		this.setAbbreviatedName("*VTDCT");
		this.setFullSyntax("*VTDISCONNECT");
		this.setAbbreviatedSyntax("*VTDCT");
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
