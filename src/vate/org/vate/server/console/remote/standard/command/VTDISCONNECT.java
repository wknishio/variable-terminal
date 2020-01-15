package org.vate.server.console.remote.standard.command;

import org.vate.console.VTConsole;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDISCONNECT extends VTServerStandardRemoteConsoleCommandProcessor
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
		VTConsole.print("\rVT>Client disconnecting...\nVT>");
		connection.closeSockets();
	}

	public void close()
	{
		
	}
}
