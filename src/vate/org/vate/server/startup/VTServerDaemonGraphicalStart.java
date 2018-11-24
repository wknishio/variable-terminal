package org.vate.server.startup;

import java.awt.GraphicsEnvironment;

import org.vate.console.VTConsole;
import org.vate.server.VTServer;

public class VTServerDaemonGraphicalStart
{
	public static final void main(String[] args)
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			VTConsole.setGraphical(true);
		}
		VTConsole.setDaemon(true);
		VTServer server = new VTServer();
		server.setDaemon(true);
		
		if (args.length >= 1)
		{
			try
			{
				server.parseParameters(args);
			}
			catch (Throwable e)
			{
				System.exit(-1);
			}
			// server.initialize();
			server.start();
		}
		else
		{
			// server.initialize();
			server.start();
		}
	}
}