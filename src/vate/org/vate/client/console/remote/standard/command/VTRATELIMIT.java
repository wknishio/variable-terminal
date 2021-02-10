package org.vate.client.console.remote.standard.command;

import org.vate.VT;
import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;

public class VTRATELIMIT extends VTClientStandardRemoteConsoleCommandProcessor
{
	public VTRATELIMIT()
	{
		this.setFullName("*VTRATELIMIT");
		this.setAbbreviatedName("*VTRL");
		this.setFullSyntax("*VTRATELIMIT [RATE] [SENSE]");
		this.setAbbreviatedSyntax("*VTRL [RT] [SE]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length == 1)
		{
			long rate = connection.getRateInBytesPerSecond();
			if (rate > 0)
			{
				VTConsole.print("\nVT>Connection upload rate limit: [" + rate + "] bytes per second\nVT>");
			}
			else
			{
				VTConsole.print("\nVT>Connection upload rate limit: [Unlimited] bytes per second\nVT>");
			}
			connection.getCommandWriter().write(command + "\n");
			connection.getCommandWriter().flush();
		}
		else if (parsed.length == 2)
		{
			long rate = 0;
			try
			{
				rate = Long.parseLong(parsed[1]);
				if (rate <= 0)
				{
					rate = 0;
				}
				else if (rate < ((VT.VT_NETWORK_PACKET_SIZE) + 8) * 2)
				{
					rate = ((VT.VT_NETWORK_PACKET_SIZE) + 8) * 2;
				}
				connection.setRateInBytesPerSecond(rate);
				if (rate > 0)
				{
					VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
				}
				else
				{
					VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
				}
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
			}
			catch (NumberFormatException e)
			{
				VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
			}
		}
		else if (parsed.length >= 3)
		{
			long rate = 0;
			try
			{
				rate = Long.parseLong(parsed[1]);
				if (rate <= 0)
				{
					rate = 0;
				}
				else if (rate < ((VT.VT_NETWORK_PACKET_SIZE) + 8) * 2)
				{
					rate = ((VT.VT_NETWORK_PACKET_SIZE) + 8) * 2;
				}
				if (parsed[2].toUpperCase().startsWith("D"))
				{
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
				else
				{
					connection.setRateInBytesPerSecond(rate);
					if (rate > 0)
					{
						VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
					}
					else
					{
						VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
					}
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
			}
			catch (NumberFormatException e)
			{
				VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
			}
		}
	}
	
	public void close()
	{
		
	}
}
