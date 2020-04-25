package org.vate.server.startup;

// import org.vate.VT;
import java.awt.GraphicsEnvironment;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.VTServer;

public class VTServerGraphicalStart
{
	public static void main(String[] args)
	{
		VTConsole.setLanterna(true);
		if (!GraphicsEnvironment.isHeadless())
		{
			VTConsole.setGraphical(true);
		}
		VTConsole.setRemoteIcon(true);
		
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
				VTConsole.setTitle("Variable-Terminal Server " + VT.VT_VERSION + " - Console");
				VTConsole.print(VTHelpManager.printModeParameterHelp());
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
			VTServer server = new VTServer();
			try
			{
				server.parseParameters(args);
			}
			catch (Throwable e)
			{
				System.exit(-1);
			}
			// server.initialize();
			server.setDaemon(false);
			server.start();
		}
		else
		{
			VTServer server = new VTServer();
			// server.initialize();
			// server.configure();
			server.setDaemon(false);
			server.start();
		}
	}
}