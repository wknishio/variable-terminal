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
				connection.getResultWriter().write("\nVT>Opening remote shell...\nVT>");
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
						connection.getResultWriter().write("\nVT>Closing remote shell...\nVT>");
						connection.getResultWriter().flush();
						session.setStoppingShell(true);
						session.stopShell();
						session.tryStopShellThreads();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Remote shell already closed!\nVT>");
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
					String[] nextShell = new String[parsed.length - 2];
					for (int i = 0; i < nextShell.length; i++)
					{
						nextShell[i] = parsed[i + 2];
					}
					connection.getResultWriter().write("\nVT>Defining remote shell to: " + Arrays.toString(nextShell) + "");
					connection.getResultWriter().flush();
					session.setShellBuilder(nextShell, null, null);
					session.restartShell();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Defining remote shell to: [Default]");
					connection.getResultWriter().flush();
					session.setShellBuilder(null, null, null);
					session.restartShell();
				}
			}
			else if (parsed[1].toUpperCase().contains("E"))
			{
				if (parsed.length >= 3 && (parsed[2].length() > 0))
				{
					String encoding = parsed[2];
					if (session.setShellEncoding(encoding))
					{
						connection.getResultWriter().write("\nVT>Defining remote shell encoding to: [" + encoding + "]");
						connection.getResultWriter().flush();
						session.restartShell();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid remote shell encoding: [" + encoding + "]");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					session.setShellEncoding(null);
					connection.getResultWriter().write("\nVT>Defining remote shell encoding to: [Default]");
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
