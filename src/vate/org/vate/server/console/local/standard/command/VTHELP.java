package org.vate.server.console.local.standard.command;

import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTHELP extends VTServerStandardLocalConsoleCommandProcessor
{
	public VTHELP()
	{
		this.setFullName("*VTHELP");
		this.setAbbreviatedName("*VTHLP");
		this.setFullSyntax("*VTHELP [COMMAND]");
		this.setAbbreviatedSyntax("*VTHLP [CMD]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length == 1)
		{
			VTConsole.print(VTHelpManager.getMainHelpForServerCommands());
		}
		else if (parsed.length > 1)
		{
			VTConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]));
		}
	}

	public void close()
	{
		
	}
}
