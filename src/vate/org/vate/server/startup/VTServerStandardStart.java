package org.vate.server.startup;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.server.VTServer;

public class VTServerStandardStart
{
	public static void main(String[] args)
	{
		VTConsole.setGraphical(false);
		
		if (args.length >= 1)
		{
			VTServer server = new VTServer();
			try
			{
				server.parseParameters(args);
			}
			catch (Throwable e)
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal Server " + VT.VT_VERSION + " - Console");
				VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
				System.exit(-1);
			}
			// server.initialize();
			server.setDaemon(false);
			server.start();
		}
		else
		{
			VTServer server = new VTServer();
			server.setDaemon(false);
			// server.initialize();
			// server.configure();
			server.start();
		}
	}
}