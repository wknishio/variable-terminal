package org.vate.server.startup;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.VTServer;

public class VTServerDaemonStandardStart
{
	public static final void main(String[] args)
	{
		VTConsole.setLanterna(true);
		VTConsole.setGraphical(false);
		VTGraphicalConsole.setRemoteIcon(true);
		VTConsole.setDaemon(true);
		VTServer server = new VTServer();
		server.setDaemon(true);
		
		if (args.length >= 1)
		{
			boolean help = false;
			for (int i = 0; i < args.length; i++)
			{
				if ("-H".equalsIgnoreCase(args[i]))
				{
					help = true;
				}
			}
			if (help)
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal Server " + VT.VT_VERSION + " - Console");
				VTConsole.print(VTHelpManager.printManualParameterHelp());
				VTConsole.print(VTHelpManager.printConnnectionParametersHelp());
				if (VTConsole.isGraphical())
				{
					try
					{
						VTConsole.readLine();
					}
					catch (Throwable e)
					{
						
					}
				}
				//VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Mode parameter:" + "\nVT>c(client)|s(server)|d(daemon)" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
				System.exit(0);
			}
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