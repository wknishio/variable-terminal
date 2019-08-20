package org.vate.server.console.remote.standard.command;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBROWSE extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTBROWSE()
	{
		this.setFullName("*VTBROWSE");
		this.setAbbreviatedName("*VTBRWS");
		this.setFullSyntax("*VTBROWSE <URI>");
		this.setAbbreviatedSyntax("*VTBRWS <URI>");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length >= 2)
		{
			try
			{
				Class.forName("java.awt.Desktop");
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
				{
					desktop.browse(new URI(parsed[1]));
					connection.getResultWriter().write("\nVT>Browse operation executed!\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Browse operation not supported!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			catch (SecurityException e)
			{
				connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
				connection.getResultWriter().flush();
			}
			catch (IllegalArgumentException e)
			{
				connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
				connection.getResultWriter().flush();
			}
			catch (IOException e)
			{
				connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
				connection.getResultWriter().flush();
			}
			catch (Throwable e)
			{
				connection.getResultWriter().write("\nVT>Browse operation not supported!\nVT>");
				connection.getResultWriter().flush();
			}
		}
		else
		{
			connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
			connection.getResultWriter().flush();
		}
	}

	public void close()
	{
		
	}
}
