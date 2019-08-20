package org.vate.client.console.remote.standard.command;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTTERMINATE extends VTClientStandardRemoteConsoleCommandProcessor
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
		connection.getCommandWriter().write(command + "\n");
		connection.getCommandWriter().flush();
		VTConsole.print("\nVT>Finalizing server...");
	}
	
	public void close()
	{
		
	}
}
