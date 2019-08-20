package org.vate.server.console.remote.standard.command;

import java.util.Arrays;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSHELL extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTSHELL()
	{
		this.setFullName("*VTSHELL");
		this.setAbbreviatedName("*VTSH");
		this.setFullSyntax("*VTSHELL <OPTIONS>");
		this.setAbbreviatedSyntax("*VTSH <OP>");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length >= 2)
		{
			if (parsed[1].toUpperCase().contains("O"))
			{
				connection.getResultWriter().write("\nVT>Opening external shell...\nVT>");
				connection.getResultWriter().flush();
				session.setRestartingShell(true);
				session.restartShell();
			}
			else if (parsed[1].toUpperCase().contains("C"))
			{
				synchronized (session.getShellExitListener())
				{
					if (!session.getShellExitListener().isStopped() && session.getShellExitListener().aliveThread())
					{
						connection.getResultWriter().write("\nVT>Closing external shell...\nVT>");
						connection.getResultWriter().flush();
						session.setStoppingShell(true);
						session.stopShell();
						session.tryStopShellThreads();
					}
					else
					{
						connection.getResultWriter().write("\nVT>External shell already closed!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				session.waitShell();
				session.waitShellThreads();
			}
			else if (parsed[1].toUpperCase().contains("D"))
			{
				if (parsed.length >= 3 && (parsed[2].length() > 0))
				{
					String[] shell = new String[parsed.length - 2];
					for (int i = 0; i < shell.length; i++)
					{
						shell[i] = parsed[i + 1];
					}
					connection.getResultWriter().write("\nVT>Defining external shell to: [" + Arrays.toString(shell) + "]");
					connection.getResultWriter().flush();
					session.setShellBuilder(shell, null, null);
					session.restartShell();
				}
				else
				{
					session.setShellBuilder(null, null, null);
					connection.getResultWriter().write("\nVT>Defining external shell to: [Default]");
					connection.getResultWriter().flush();
					
					session.restartShell();
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

	public void close()
	{
		
	}
}
