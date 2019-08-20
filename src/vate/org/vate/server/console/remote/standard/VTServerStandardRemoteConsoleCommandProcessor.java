package org.vate.server.console.remote.standard;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.VTServerRemoteConsoleCommandProcessor;

public abstract class VTServerStandardRemoteConsoleCommandProcessor extends VTServerRemoteConsoleCommandProcessor
{
	public VTServerStandardRemoteConsoleCommandProcessor()
	{
		
	}
	
	public String help(String name)
	{
		return VTHelpManager.getHelpForClientCommand(name);
	}
}