package org.vate.server.console.remote.standard.command;

import org.vate.console.VTConsole;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTTERMINATE extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTTERMINATE()
	{
		this.setFullName("*VTTERMINATE");
		this.setAbbreviatedName("*VTTMNT");
		this.setFullSyntax("*VTTERMINATE");
		this.setAbbreviatedSyntax("*VTTMNT");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		VTConsole.print("\rVT>Client finalizing server...\nVT>");
		connection.closeSockets();
		System.exit(0);
	}

	public void close()
	{
		
	}
}
