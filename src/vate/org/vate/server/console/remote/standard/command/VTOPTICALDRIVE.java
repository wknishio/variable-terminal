package org.vate.server.console.remote.standard.command;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTOPTICALDRIVE extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTOPTICALDRIVE()
	{
		this.setFullName("*VTOPTICALDRIVE");
		this.setAbbreviatedName("*VTOPDR");
		this.setFullSyntax("*VTOPTICALDRIVE <MODE>");
		this.setAbbreviatedSyntax("*VTOPDR <MD>");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		synchronized (session.getCDOperation())
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (parsed.length >= 2)
			{
				if (parsed[1].toUpperCase().startsWith("O"))
				{
					if (session.getCDOperation().isFinished())
					{
						session.getCDOperation().joinThread();
					}
					if (!session.getCDOperation().aliveThread())
					{
						session.getCDOperation().setFinished(false);
						session.getCDOperation().setOpen(true);
						session.getCDOperation().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else if (parsed[1].toUpperCase().startsWith("C"))
				{
					if (session.getCDOperation().isFinished())
					{
						session.getCDOperation().joinThread();
					}
					if (!session.getCDOperation().aliveThread())
					{
						session.getCDOperation().setFinished(false);
						session.getCDOperation().setOpen(false);
						session.getCDOperation().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
				connection.getResultWriter().flush();
			}
		}
	}

	public void close()
	{
		
	}
}
