package org.vate.server.console.remote.standard.command;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.vate.console.VTConsole;
import org.vate.graphics.message.VTGraphicsMessager;
import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENALERT extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTSCREENALERT()
	{
		this.setFullName("*VTSCREENALERT");
		this.setAbbreviatedName("*VTSA");
		this.setFullSyntax("*VTSCREENALERT <[TITLE/]ALERT> [DISPLAY]");
		this.setAbbreviatedSyntax("*VTSA <[TI/]ALT> [DP]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			if (parsed.length == 2)
			{
				// splitCommand[1] =
				// StringEscapeUtils.unescapeJava(splitCommand[1]);
				if (parsed[1].indexOf('/') < 0)
				{
					VTGraphicsMessager.showAlert(VTConsole.getFrame(), "Variable-Terminal Server", parsed[1]);
					session.getServer().displayTrayIconMessage("Variable-Terminal Server", "["+ parsed[1] + "]");
				}
				else
				{
					String title = parsed[1].substring(0, parsed[1].indexOf('/'));
					String message = parsed[1].substring(parsed[1].indexOf('/') + 1);
					VTGraphicsMessager.showAlert(VTConsole.getFrame(), title, message);
					session.getServer().displayTrayIconMessage(title, "[" + message + "]");
				}
				connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
				connection.getResultWriter().flush();
			}
			else if (parsed.length >= 3)
			{
				try
				{
					// splitCommand[1] =
					// StringEscapeUtils.unescapeJava(splitCommand[1]);
					// splitCommand[2] =
					// StringEscapeUtils.unescapeJava(splitCommand[2]);
					final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
					int number = Integer.parseInt(parsed[2]);
					if (number < devices.length)
					{
						if (number <= -1)
						{
							number = 0;
						}
						if (parsed[1].indexOf('/') < 0)
						{
							VTGraphicsMessager.showAlert(devices[number], VTConsole.getFrame(), "Variable-Terminal Server", parsed[1]);
							session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[" + parsed[1] + "]");
						}
						else
						{
							String title = parsed[1].substring(0, parsed[1].indexOf('/'));
							String message = parsed[1].substring(parsed[1].indexOf('/') + 1);
							VTGraphicsMessager.showAlert(devices[number], VTConsole.getFrame(), title, message);
							session.getServer().displayTrayIconMessage(title, "[" + message + "]");
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (NumberFormatException e)
				{
					connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
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
			connection.getResultWriter().write("\nVT>Graphical alert not supported in server!\nVT>");
			connection.getResultWriter().flush();
		}
	}

	public void close()
	{
		
	}
}
