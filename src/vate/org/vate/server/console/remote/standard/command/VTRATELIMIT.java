package org.vate.server.console.remote.standard.command;

import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTRATELIMIT extends VTServerStandardRemoteConsoleCommandProcessor
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
				connection.getResultWriter().write("\nVT>Connection download rate limit: [" + rate + "] bytes per second\nVT>");
				connection.getResultWriter().flush();
			}
			else
			{
				connection.getResultWriter().write("\nVT>Connection download rate limit: [Unlimited] bytes per second\nVT>");
				connection.getResultWriter().flush();
			}
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
				else if (rate < ((1024 + 8) * 8))
				{
					rate = ((1024 + 8) * 8);
				}
				connection.setRateInBytesPerSecond(rate);
				if (rate > 0)
				{
					connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
					connection.getResultWriter().flush();
				}
			}
			catch (NumberFormatException e)
			{
				
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
				else if (rate < ((1024 + 8) * 8))
				{
					rate = ((1024 + 8) * 8);
				}
				if (!parsed[2].toUpperCase().startsWith("U"))
				{
					connection.setRateInBytesPerSecond(rate);
					if (rate > 0)
					{
						connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
						connection.getResultWriter().flush();
					}
				}
			}
			catch (NumberFormatException e)
			{
				
			}
		}
	}

	public void close()
	{
		
	}
}
