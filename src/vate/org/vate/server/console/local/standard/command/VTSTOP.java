package org.vate.server.console.local.standard.command;

import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTSTOP extends VTServerStandardLocalConsoleCommandProcessor
{	
	public VTSTOP()
	{
		this.setFullName("*VTSTOP");
		this.setAbbreviatedName("*VTSTP");
		this.setFullSyntax("*VTSTOP");
		this.setAbbreviatedSyntax("*VTSTP");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		System.exit(0);
	}

	public void close()
	{
		
	}
}
