package org.vate.server.console.remote.standard.command;

import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTNETWORKINTERFACES extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTNETWORKINTERFACES()
	{
		this.setFullName("*VTNETWORKINTERFACES");
		this.setAbbreviatedName("*VTNTIS");
		this.setFullSyntax("*VTNETWORKINTERFACES");
		this.setAbbreviatedSyntax("*VTNTIS");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		synchronized (session.getNetworkInterfaceResolver())
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (session.getNetworkInterfaceResolver().isFinished())
			{
				session.getNetworkInterfaceResolver().joinThread();
			}
			if (!session.getNetworkInterfaceResolver().aliveThread())
			{
				session.getNetworkInterfaceResolver().setFinished(false);
				session.getNetworkInterfaceResolver().startThread();
			}
			else
			{
				connection.getResultWriter().write("\nVT>Another network interface search is still running!\nVT>");
				connection.getResultWriter().flush();
			}
		}
	}

	public void close()
	{
		
	}
}