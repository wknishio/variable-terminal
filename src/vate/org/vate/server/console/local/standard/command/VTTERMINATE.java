package org.vate.server.console.local.standard.command;

import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTTERMINATE extends VTServerStandardLocalConsoleCommandProcessor
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
		System.exit(0);
	}

	public void close()
	{
		
	}
}
