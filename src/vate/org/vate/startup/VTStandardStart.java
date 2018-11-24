package org.vate.startup;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.console.VTConsole;
import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.server.VTServer;

public final class VTStandardStart
{
	private static String option;
	
	public static final void main(String args[])
	{
		VTConsole.setGraphical(false);
		
		if (args.length == 0)
		{
			VTConsole.initialize();
			VTConsole.clear();
			VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
			VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + "\nVT>Copyright (c) " + VT.VT_YEAR + " - wknishio@gmail.com\n"
			+ "VT>This software is under MIT license, see license.txt!\n"
			+ "VT>This software comes with no warranty, use at your own risk!\n");
			
			if (VTConsole.isGraphical())
			{
				VTGraphicalStartDialog dialog = new VTGraphicalStartDialog(VTGraphicalConsole.getFrame());
				dialog.setVisible(true);
				if (dialog.getMode() == 1)
				{
					try
					{
						VTClient client = new VTClient();
						// client.initialize();
						// client.configure();
						client.start();
						return;
					}
					catch (Throwable e)
					{
						
					}
				}
				else if (dialog.getMode() == 2)
				{
					try
					{
						VTServer server = new VTServer();
						server.setDaemon(false);
						// server.initialize();
						// server.configure();
						server.start();
						return;
					}
					catch (Throwable e)
					{
						
					}
				}
			}
			
			VTConsole.print("VT>Enter the module(client as C or server as S, default:C):");
			try
			{
				option = VTConsole.readLine(true);
				if (option.toUpperCase().startsWith("S"))
				{
					VTServer server = new VTServer();
					server.setDaemon(false);
					// server.initialize();
					// server.configure();
					server.start();
				}
				else
				{
					VTClient client = new VTClient();
					// client.initialize();
					// client.configure();
					client.start();
				}
			}
			catch (Throwable e)
			{
				System.exit(0);
			}
		}
		else
		{
			if (args[0].toUpperCase().startsWith("S"))
			{
				if (args.length >= 2)
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
						VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Mode parameter:" + "\nVT>c(client)|s(server)|d(daemon)" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
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
			else if (args[0].toUpperCase().startsWith("D"))
			{
				VTConsole.setDaemon(true);
				if (args.length >= 2)
				{
					VTServer server = new VTServer();
					server.setDaemon(true);
					try
					{
						server.parseParameters(args);
					}
					catch (Throwable e)
					{
						VTConsole.initialize();
						VTConsole.clear();
						VTConsole.setTitle("Variable-Terminal Server " + VT.VT_VERSION + " - Console");
						VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Mode parameter:" + "\nVT>c(client)|s(server)|d(daemon)" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
						System.exit(-1);
					}
					// server.initialize();
					server.start();
				}
				else
				{
					VTServer server = new VTServer();
					server.setDaemon(true);
					// server.initialize();
					// server.configure();
					server.start();
				}
			}
			else if (args[0].toUpperCase().startsWith("C"))
			{
				if (args.length >= 2)
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
						VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Mode parameter:" + "\nVT>c(client)|s(server)|d(daemon)" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
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
			else
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
				VTConsole.println("VT>Invalid parameter syntax!" + "\nVT>Mode parameter:" + "\nVT>c(client)|s(server)|d(daemon)" + "\nVT>Host parameter:" + "\nVT>[connectionhost/]connectionport[;natport]" + "\nVT>Settings file parameter:" + "\nVT>settingsfile" + "\nVT>Optional parameters:" + "\nVT>[login/password]" + "\nVT>[encryptiontype;encryptionpassword]" + "\nVT>[proxytype/proxyhost/proxyport[/proxyuser/proxypassword]]" + "\nVT>[sessionslimit]");
				System.exit(-1);
			}
		}
	}
}