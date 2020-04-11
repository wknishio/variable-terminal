package org.vate.client.startup;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;

public class VTClientStandardStart
{
	public static void main(String[] args)
	{
		VTConsole.setLanterna(true);
		VTConsole.setGraphical(false);
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
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal Client " + VT.VT_VERSION + " - Console");
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
			VTClient client = new VTClient();
			try
			{
				client.parseParameters(args);
			}
			catch (Throwable e)
			{
				System.exit(-1);
			}
			// client.initialize();
			client.start();
		}
		else
		{
			VTClient client = new VTClient();
			// client.initialize();
			// client.configure();
			client.start();
		}
	}
}