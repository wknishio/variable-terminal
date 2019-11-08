package org.vate.startup;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.console.VTConsole;
import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.VTServer;

public final class VTStandardStart
{
	private static String option;
	
	public static final void main(String args[])
	{
		//VTConsole.setLanterna(true);
		VTConsole.setGraphical(false);
		VTGraphicalConsole.setRemoteIcon(true);
		
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
				VTGraphicalStartDialog dialog = new VTGraphicalStartDialog(VTConsole.getFrame());
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
			int type = 0;
			for (int i = 0; i < args.length; i++)
			{
				if ("-C".equalsIgnoreCase(args[i]))
				{
					type = 1;
				}
				if ("-S".equalsIgnoreCase(args[i]))
				{
					type = 2;
				}
				if ("-D".equalsIgnoreCase(args[i]))
				{
					type = 3;
				}
				if ("-H".equalsIgnoreCase(args[i]))
				{
					type = 4;
				}
			}
			if (type == 1)
			{
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
			else if (type == 2)
			{
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
			else if (type == 3)
			{
				VTConsole.setDaemon(true);
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
				server.setDaemon(true);
				server.start();
			}
			else if (type == 4)
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
				VTConsole.print(VTHelpManager.printManualParameterHelp());
				VTConsole.print(VTHelpManager.printApplicationParametersHelp());
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
			else
			{
				VTConsole.initialize();
				VTConsole.clear();
				VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
				VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + "\nVT>Copyright (c) " + VT.VT_YEAR + " - wknishio@gmail.com\n"
				+ "VT>This software is under MIT license, see license.txt!\n"
				+ "VT>This software comes with no warranty, use at your own risk!\n");
				
				if (VTConsole.isGraphical())
				{
					VTGraphicalStartDialog dialog = new VTGraphicalStartDialog(VTConsole.getFrame());
					dialog.setVisible(true);
					if (dialog.getMode() == 1)
					{
						try
						{
							VTClient client = new VTClient();
							// client.initialize();
							// client.configure();
							try
							{
								client.parseParameters(args);
							}
							catch (Throwable e)
							{
								
							}
							client.start();
							return;
						}
						catch (Throwable e)
						{
							// e.printStackTrace();
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
							try
							{
								server.parseParameters(args);
							}
							catch (Throwable e)
							{
								
							}
							server.start();
							return;
						}
						catch (Throwable e)
						{
							// e.printStackTrace();
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
						try
						{
							server.parseParameters(args);
						}
						catch (Throwable e)
						{
							
						}
						// server.initialize();
						// server.configure();
						server.start();
					}
					else
					{
						VTClient client = new VTClient();
						try
						{
							client.parseParameters(args);
						}
						catch (Throwable e)
						{
							
						}
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
		}
	}
}