package org.vate.client.startup;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.console.VTConsole;

public class VTClientStandardStart
{
	public static void main(String[] args)
	{
		VTConsole.setGraphical(false);
		
		if (args.length >= 1)
		{
			VTClient client = new VTClient();
			try
			{
				client.parseParameters(args);
			}
			catch (Throwable e)
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal Client " + VT.VT_VERSION + " - Console");
				VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype[/proxyuser/proxypassword]/proxyhost:proxyport]");
				try
				{
					VTConsole.readLine(false);
				}
				catch (InterruptedException e1)
				{
					
				}
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