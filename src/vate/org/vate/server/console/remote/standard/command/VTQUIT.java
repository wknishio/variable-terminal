package org.vate.server.console.remote.standard.command;

import org.vate.console.VTConsole;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTQUIT extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTQUIT()
	{
		this.setFullName("*VTQUIT");
		this.setAbbreviatedName("*VTQT");
		this.setFullSyntax("*VTQUIT");
		this.setAbbreviatedSyntax("*VTQT");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		VTConsole.print("\rVT>Client finalizing...\nVT>");
		connection.closeSockets();
	}

	public void close()
	{
		
	}
}
