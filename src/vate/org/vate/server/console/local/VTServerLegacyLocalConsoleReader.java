package org.vate.server.console.local;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.console.VTConsole;
import org.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vate.help.VTHelpManager;
import org.vate.nativeutils.VTNativeUtils;
import org.vate.server.VTServer;
import org.vate.server.connection.VTServerConnectionHandler;
import org.vate.server.connection.VTServerConnector;
import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerLegacyLocalConsoleReader extends VTTask
{
	// private String command;
	// private String[] splitCommand;
	private DateFormat firstDateTimeFormat;
	private DateFormat secondDateTimeFormat;
	private GregorianCalendar clock;
	private StringBuilder message;
	private VTServer server;
	
	public VTServerLegacyLocalConsoleReader(VTServer server)
	{
		this.server = server;
		this.firstDateTimeFormat = new SimpleDateFormat("G", Locale.ENGLISH);
		this.secondDateTimeFormat = new SimpleDateFormat("MM-dd][HH:mm:ss:SSS-z]");
		this.clock = new GregorianCalendar();
		this.message = new StringBuilder();
	}
	
	public void run()
	{
		// int p = 0;
		VTConsole.print("\rVT>Enter *VTHELP or *VTHLP to list available commands in server console\nVT>");
		while (true)
		{
			try
			{
				String line = VTConsole.readLine(true);
				String[] commands = line.split("*;");
				for (String command : commands)
				{
					executeCommand(command, null);
				}
			}
			catch (InterruptedException e)
			{
				// e.printStackTrace();
			}
			catch (Throwable e)
			{
				//e.printStackTrace();
				VTConsole.print("\rVT>Error while processing command!\nVT>");
				// e.printStackTrace(VTConsole.getSystemOut());
				// return;
				/* VTTerminal.setSystemErr(); VTTerminal.setSystemOut();
				 * VTTerminal.setSystemIn(); e.printStackTrace(); */
			}
			if (VTConsole.isDaemon())
			{
				Object waiter = VTConsole.getSynchronizationObject();
				synchronized (waiter)
				{
					while (VTConsole.isDaemon())
					{
						try
						{
							waiter.wait();
						}
						catch (Throwable e)
						{
							
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void executeStringScript(String script, Set<String> stack)
	{
		if (stack == null)
		{
			stack = new HashSet<String>();
		}
		if (script == null || script.length() < 1 || !stack.add(script))
		{
			// protection for recursion and bad script string
			return;
		}
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new StringReader(script));
			String line = "";
			while (!stopped && (line = reader.readLine()) != null)
			{
				String[] commands = line.split("*;");
				for (String command : commands)
				{
					executeCommand(command, stack);
				}
			}
		}
		catch (Throwable t)
		{
			
		}
		finally
		{
			if (script != null)
			{
				stack.remove(script);
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Throwable t)
				{
					
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void executeFileScript(File script, Set<String> stack)
	{
		if (stack == null)
		{
			stack = new HashSet<String>();
		}
		if (script == null || !script.exists() || !stack.add(script.getAbsolutePath()))
		{
			// protection for recursion and bad file paths
			return;
		}
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
			String line = "";
			while (!stopped && (line = reader.readLine()) != null)
			{
				String[] commands = line.split("*;");
				for (String command : commands)
				{
					executeCommand(command, stack);
				}
			}
		}
		catch (Throwable t)
		{
			
		}
		finally
		{
			if (script != null)
			{
				stack.remove(script.getAbsolutePath());
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Throwable t)
				{
					
				}
			}
		}
	}
	
	private void executeCommand(String command, Set<String> stack) throws Throwable
	{
		String parsed[];
		if (command != null)
		{
			if (!(command.length() == 0))
			{
				parsed = CommandLineTokenizer.tokenize(command);
				if (parsed.length < 1)
				{
					parsed = new String[] { command };
					// p = 0;
					/* for (String part : splitCommand) { splitCommand[p++] =
					 * StringEscapeUtils.unescapeJava(part); } */
				}
			}
			else
			{
				parsed = new String[] { "" };
			}
			if (parsed[0].equalsIgnoreCase("*VTMESSAGE") || parsed[0].equalsIgnoreCase("*VTMSG"))
			{
				if (command.contains("\""))
				{
					if (parsed.length >= 2)
					{
						List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
						synchronized (connections)
						{
							if (connections.size() > 0)
							{
								for (VTServerConnectionHandler connectionHandler : connections)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + parsed[1] + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												// VTTerminal.print("\rVT>Error
												// detected when sending
												// message!\nVT>");
											}
										}
									}
								}
								VTConsole.print("\rVT>Message sent to clients!\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Not connected with clients!\nVT>");
							}
						}
					}
					else if (parsed.length == 1)
					{
						List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
						synchronized (connections)
						{
							if (connections.size() > 0)
							{
								for (VTServerConnectionHandler connectionHandler : connections)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: []\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												// VTTerminal.print("\rVT>Error
												// detected when sending
												// message!\nVT>");
											}
										}
									}
								}
								VTConsole.print("\rVT>Message sent to clients!\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Not connected with clients!\nVT>");
							}
						}
					}
				}
				else
				{
					if (command.length() >= 12 && command.charAt(11) == ' ')
					{
						// command = StringEscapeUtils.unescapeJava(command);
						List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
						synchronized (connections)
						{
							if (connections.size() > 0)
							{
								for (VTServerConnectionHandler connectionHandler : connections)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + command.substring(12) + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												// VTTerminal.print("\rVT>Error
												// detected when sending
												// message!\nVT>");
											}
										}
									}
								}
								VTConsole.print("\rVT>Message sent to clients!\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Not connected with clients!\nVT>");
							}
						}
					}
					else if (command.length() >= 8 && command.charAt(7) == ' ')
					{
						// command = StringEscapeUtils.unescapeJava(command);
						List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
						synchronized (connections)
						{
							if (connections.size() > 0)
							{
								for (VTServerConnectionHandler connectionHandler : connections)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + command.substring(8) + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												// VTTerminal.print("\rVT>Error
												// detected when sending
												// message!\nVT>");
											}
										}
									}
								}
								VTConsole.print("\rVT>Message sent to clients!\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Not connected with clients!\nVT>");
							}
						}
					}
					else
					{
						List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
						synchronized (connections)
						{
							if (connections.size() > 0)
							{
								for (VTServerConnectionHandler connectionHandler : connections)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: []\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												// VTTerminal.print("\rVT>Error
												// detected when sending
												// message!\nVT>");
											}
										}
									}
								}
								VTConsole.print("\rVT>Message sent to clients!\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Not connected with clients!\nVT>");
							}
						}
					}
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTSTOP") || parsed[0].equalsIgnoreCase("*VTSTP"))
			{
				System.exit(0);
			}
			else if (parsed[0].equalsIgnoreCase("*VTSESSIONS") || parsed[0].equalsIgnoreCase("*VTSNS"))
			{
				int i = 0;
				message.setLength(0);
				List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
				synchronized (connections)
				{
					if (connections.size() > 0)
					{
						message.append("\rVT>List of current client connections on server:\nVT>");
						for (VTServerConnectionHandler handler : connections)
						{
							message.append("\nVT>Number: [" + i++ + "]");
							message.append("\nVT>Authenticated: [" + (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
							message.append("\nVT>Login: [" + (handler.getSessionHandler().getLogin() != null ? handler.getSessionHandler().getLogin() : "") + "]");
							InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
							if (address != null)
							{
								message.append("\nVT>Host address: [" + address.getHostAddress() +
								// "]\nVT>Host name: [" +
								// address.getHostName() +
								// "]\nVT>Canonical host name: [" +
								// address.getCanonicalHostName() +
								"]\nVT>");
							}
						}
						message.append("\nVT>End of current client connections list\nVT>");
						VTConsole.print(message.toString());
					}
					else
					{
						VTConsole.print("\rVT>Not connected with clients!\nVT>");
					}
				}
				VTConsole.print("\nVT>");
			}
			else if (parsed[0].equalsIgnoreCase("*VTHELP"))
			{
				if (parsed.length == 1)
				{
					VTConsole.print(VTHelpManager.getMainHelpForServerCommands());
				}
				else if (parsed.length > 1)
				{
					VTConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTHLP"))
			{
				if (parsed.length == 1)
				{
					VTConsole.print(VTHelpManager.getMinHelpForServerCommands());
				}
				else if (parsed.length > 1)
				{
					VTConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTCLEAR") || parsed[0].equalsIgnoreCase("*VTCLR"))
			{
				VTConsole.clear();
				VTConsole.print("VT>");
			}
			else if (parsed[0].equalsIgnoreCase("*VTNETWORKS") || parsed[0].equalsIgnoreCase("*VTNTS"))
			{
				message.setLength(0);
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				message.append("\rVT>List of network interfaces on server:\nVT>");
				if (networkInterfaces != null && networkInterfaces.hasMoreElements())
				{
					while (networkInterfaces.hasMoreElements())
					{
						NetworkInterface networkInterface = networkInterfaces.nextElement();
						Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
						if (!addresses.hasMoreElements())
						{
							continue;
						}
						message.append("\nVT>Name: [" + networkInterface.getName() + "]"
						+ "\nVT>Display name: [" + networkInterface.getDisplayName() + "]");
						
						try
						{
//							byte[] hardwareAddress = networkInterface.getHardwareAddress();
//							if (hardwareAddress != null && hardwareAddress.length > 0)
//							{
//								message.append("\nVT>Hardware address: [");
//								for (int i = 0; i < hardwareAddress.length; i++)
//								{
//									message.append(String.format("%02X%s", hardwareAddress[i], (i < hardwareAddress.length - 1) ? "-" : ""));
//								}
//								message.append("]");
//							}
						}
						catch (Throwable t)
						{
							//Hardware address available in 1.6 and beyond but we support 1.5
						}
						
						while (addresses.hasMoreElements())
						{
							InetAddress address = addresses.nextElement();
							message.append("\nVT>Host address: [" + address.getHostAddress() + "]");
							// "]\nVT>Host name: [" + address.getHostName() +
							// "]\nVT>Canonical host name: [" +
							// address.getCanonicalHostName() + "]");
						}
						message.append("\nVT>");
					}
					message.append("\nVT>End of network interfaces list\nVT>");
				}
				else
				{
					message.append("\rVT>No network interfaces found on server!\nVT>");
				}
				VTConsole.print(message.toString());
			}
			else if (parsed[0].equalsIgnoreCase("*VTDISPLAYS") || parsed[0].equalsIgnoreCase("*VTDPS"))
			{
				message.setLength(0);
				int count = 0;
				GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
				if (devices != null && devices.length > 0)
				{
					message.append("\rVT>List of graphical display devices on server:\nVT>");
					for (GraphicsDevice device : devices)
					{
						DisplayMode mode = device.getDisplayMode();
						message.append("\nVT>Number: [" + (count++) + "]");
						message.append("\nVT>ID: [" + device.getIDstring() + "]");
						message.append("\nVT>Mode: [" + mode.getWidth() + "x" + mode.getHeight() + "]");
						Rectangle bounds = device.getDefaultConfiguration().getBounds();
						message.append("\nVT>Origin: [X:" + bounds.x + " Y:" + bounds.y + "]");
						message.append("\nVT>");
					}
					message.append("\nVT>End of graphical display devices list\nVT>");
				}
				else
				{
					message.append("\rVT>No graphical display devices found on server!\nVT>");
				}
				VTConsole.print(message.toString());
			}
			/* else if (splitCommand[0].equalsIgnoreCase("*VTAUDIODEVICES") ||
			 * splitCommand[0].equalsIgnoreCase("*VTAUDVS")) {
			 * message.setLength(0); int count = 0; Mixer.Info[] mixers =
			 * AudioSystem.getMixerInfo(); if (mixers != null && mixers.length >
			 * 0) {
			 * message.append("\rVT>List of audio devices on server:\nVT>");
			 * for (Mixer.Info mixerInfo : mixers) { Mixer mixer =
			 * AudioSystem.getMixer(mixerInfo); message.append("\nVT>Number: "
			 * + (count++)); message.append("\nVT>Name: [" +
			 * mixerInfo.getName() + "]"); //message.append("\nVT>Version: [" +
			 * mixerInfo.getVersion() + "]");
			 * message.append("\nVT>Description: [" +
			 * mixerInfo.getDescription() + "]"); Info[] targetLines =
			 * mixer.getTargetLineInfo(); Info[] sourceLines =
			 * mixer.getSourceLineInfo(); for (Info targetLine : targetLines) {
			 * message.append("\nVT>Input: " + targetLine.toString()); } for
			 * (Info sourceLine : sourceLines) { message.append("\nVT>Output: "
			 * + sourceLine.toString()); }
			 * message.append("\nVT>"); }
			 * message.append("\nVT>End of audio devices list\nVT>"); } else {
			 * message.append("\rVT>No audio devices found on server!\nVT>");
			 * } VTConsole.print(message.toString()); } */
			else if (parsed[0].equalsIgnoreCase("*VTFILEROOTS") || parsed[0].equalsIgnoreCase("*VTFRTS"))
			{
				message.setLength(0);
				File[] roots = File.listRoots();
				message.append("\rVT>List of file system roots on server:\nVT>");
				for (File root : roots)
				{
					message.append("\nVT>Canonical path: [" + root.getCanonicalPath() + "]");
				}
				message.append("\nVT>\nVT>End of file system roots list\nVT>");
				VTConsole.print(message.toString());
			}
			else if (parsed[0].equalsIgnoreCase("*VTPRINTERS") || parsed[0].equalsIgnoreCase("*VTPRS"))
			{
				message.setLength(0);
				PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
				PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
				if (printServices.length > 0)
				{
					int i = 0;
					message.append("\rVT>List of print services on server:\nVT>");
					for (PrintService printService : printServices)
					{
						// message.append("\nVT>Name: [" +
						// printService.getName() + "]");
						message.append("\nVT>Number: [" + i++ + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
						/* for (DocFlavor flavor :
						 * printService.getSupportedDocFlavors()) {
						 * message.append("\nVT>Flavor: " +
						 * flavor.getMimeType()); } */
						message.append("\nVT>");
					}
					message.append("\nVT>End of print services list\nVT>");
					
					VTConsole.print(message.toString());
				}
				else
				{
					VTConsole.print("\rVT>No print services found on server!\nVT>");
				}
			}
			/* else if
			 * (splitCommand[0].equalsIgnoreCase("*VTDEFAULTPRINTSERVICE") ||
			 * splitCommand[0].equalsIgnoreCase("*VTDPSVC")) { PrintService
			 * defaultPrintService =
			 * PrintServiceLookup.lookupDefaultPrintService(); if
			 * (defaultPrintService != null) { VTTerminal.print("\rVT>Name: ["
			 * + defaultPrintService.getName() + "]\nVT>"); } else {
			 * VTTerminal.print("\rVT>Default print service not found!" +
			 * "\nVT>"); } } */
			else if (parsed[0].equalsIgnoreCase("*VTVARIABLE") || parsed[0].equalsIgnoreCase("*VTVAR"))
			{
				if (parsed.length == 1)
				{
					message.setLength(0);
					message.append("\rVT>List of environment variables on server:\nVT>");
					for (Entry<String, String> variable : VTNativeUtils.getvirtualenv().entrySet())
					{
						message.append("\nVT>[" + variable.getKey() + "]=[" + variable.getValue() + "]");
					}
					message.append("\nVT>\nVT>End of environment variables list\nVT>");
					VTConsole.print(message.toString());
				}
				else if (parsed.length == 2)
				{
					String value = VTNativeUtils.getvirtualenv(parsed[1]);
					if (value != null)
					{
						VTConsole.print("\rVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>Environment variable [" + parsed[1] + "] not found on server!\nVT>");
					}
				}
				else if (parsed.length >= 3)
				{
					if (VTNativeUtils.putvirtualenv(parsed[1], parsed[2]) == 0)
					{
						VTConsole.print("\rVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>Environment variable [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTPROPERTY") || parsed[0].equalsIgnoreCase("*VTPROP"))
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (parsed.length == 1)
				{
					message.setLength(0);
					message.append("\rVT>JVM properties on server:\nVT>");
					for (Entry<Object, Object> property : System.getProperties().entrySet())
					{
						message.append("\nVT>[" + property.getKey().toString() + "]=[" + property.getValue().toString() + "]");
					}
					message.append("\nVT>\nVT>End of JVM properties list\nVT>");
					VTConsole.print(message.toString());
				}
				else if (parsed.length == 2)
				{
					String value = System.getProperty(parsed[1]);
					if (value != null)
					{
						VTConsole.print("\rVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>JVM property [" + parsed[1] + "] not found on server!\nVT>");
					}
				}
				else if (parsed.length >= 3)
				{
					try
					{
						System.setProperty(parsed[1], parsed[2]);
						VTConsole.print("\rVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
					}
					catch (Throwable e)
					{
						VTConsole.print("\rVT>JVM property [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTDISCONNECT") || parsed[0].equalsIgnoreCase("*VTDCT"))
			{
				if (parsed.length == 1)
				{
					List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							VTConsole.print("\rVT>Disconnecting all clients from server...\nVT>");
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								connectionHandler.getConnection().closeSockets();
							}
							VTConsole.print("\rVT>Disconnected all clients from server!\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Not connected with clients!\nVT>");
						}
					}
				}
				else if (parsed.length >= 2)
				{
					try
					{
						int number = Integer.parseInt(parsed[1]);
						if (number >= 0)
						{
							List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
							synchronized (connections)
							{
								if (connections.size() > 0)
								{
									if (connections.size() >= number)
									{
										VTConsole.print("\rVT>Disconnecting client of number [" + number + "] from server...\nVT>");
										connections.get(number).getConnection().closeSockets();
										VTConsole.print("\rVT>Disconnected client of number [" + number + "] from server!\nVT>");
									}
									else
									{
										VTConsole.print("\rVT>Client number [" + parsed[1] + "] is not valid!\nVT>");
									}
								}
								else
								{
									VTConsole.print("\rVT>Not connected with clients!\nVT>");
								}
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					catch (NumberFormatException e)
					{
						VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTACCESS") || parsed[0].equalsIgnoreCase("VTAC"))
			{
				if (parsed.length == 1)
				{
					message.setLength(0);
					String hostAddress = server.getServerConnector().getAddress();
					Integer port = server.getServerConnector().getPort();
					Integer natPort = server.getServerConnector().getNatPort();
					String proxyType = server.getServerConnector().getProxyType();
					String proxyAddress = server.getServerConnector().getProxyAddress();
					Integer proxyPort = server.getServerConnector().getProxyPort();
					String proxyUser = server.getServerConnector().getProxyUser();
					String proxyPassword = server.getServerConnector().getProxyPassword();
					String encryptionType = server.getServerConnector().getEncryptionType();
					String encryptionPassword = "";
					int sessionsLimit = server.getServerConnector().getSessionsLimit();
					if (server.getServerConnector().getEncryptionKey() != null)
					{
						encryptionPassword = new String(server.getServerConnector().getEncryptionKey(), "UTF-8");
					}
					message.append("\rVT>List of connection settings on server:\nVT>");
					
					if (server.getServerConnector().isPassive())
					{
						message.append("\nVT>Connection mode(CM): [Passive]");
					}
					else
					{
						message.append("\nVT>Connection mode(CM): [Active]");
					}
					message.append("\nVT>Connection host address(CH): [" + hostAddress + "]");
					message.append("\nVT>Connection host port(CP): [" + port + "]");
					if (natPort != null)
					{
						message.append("\nVT>Connection nat port(NP): [" + natPort + "]");
					}
					else
					{
						message.append("\nVT>Connection nat port(NP): []");
					}
					if (proxyType == null)
					{
						message.append("\nVT>Proxy type(PT): [None]");
					}
					else if (proxyType.toUpperCase().startsWith("H"))
					{
						message.append("\nVT>Proxy type(PT): [HTTP]");
					}
					else if (proxyType.toUpperCase().startsWith("S"))
					{
						message.append("\nVT>Proxy type(PT): [SOCKS]");
					}
					else
					{
						message.append("\nVT>Proxy type(PT): [None]");
					}
					message.append("\nVT>Proxy host address(PH): [" + proxyAddress + "]");
					if (proxyPort != null)
					{
						message.append("\nVT>Proxy host port(PP): [" + proxyPort + "]");
					}
					else
					{
						message.append("\nVT>Proxy host port(PP): []");
					}
					if (server.getServerConnector().isUseProxyAuthentication())
					{
						message.append("\nVT>Proxy authentication(PA): [Enabled]");
					}
					else
					{
						message.append("\nVT>Proxy authentication(PA): [Disabled]");
					}
					message.append("\nVT>Proxy user(PU): [" + proxyUser + "]");
					message.append("\nVT>Proxy password(PS): [" + proxyPassword + "]");
					if (encryptionType.toUpperCase().startsWith("R"))
					{
						message.append("\nVT>Encryption type(ET): RC4");
					}
					else if (encryptionType.toUpperCase().startsWith("A"))
					{
						message.append("\nVT>Encryption type(ET): [AES]");
					}
					else
					{
						message.append("\nVT>Encryption type(ET): [None]");
					}
					message.append("\nVT>Encryption password(ES): [" + encryptionPassword + "]");
					message.append("\nVT>Sessions limit(SL): [" + sessionsLimit + "]");
					message.append("\nVT>\nVT>End of connection settings list on server\nVT>");
					VTConsole.print(message.toString());
					message.setLength(0);
				}
				else if (parsed.length >= 2)
				{
					if (parsed[1].equalsIgnoreCase("SF"))
					{
						if (parsed.length == 2)
						{
							try
							{
								server.saveServerSettingsFile("variable-terminal-server.properties");
								VTConsole.print("\rVT>Saved settings file:[variable-terminal-server.properties]\nVT>");
							}
							catch (Throwable t)
							{
								VTConsole.print("\rVT>Failed to save settings file:[variable-terminal-server.properties]\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							try
							{
								server.saveServerSettingsFile(parsed[2]);
								VTConsole.print("\rVT>Saved settings file:[" + parsed[2] + "]\nVT>");
							}
							catch (Throwable t)
							{
								VTConsole.print("\rVT>Failed to save settings file:[" + parsed[2] + "]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("LF"))
					{
						if (parsed.length == 2)
						{
							boolean ok = false;
							try
							{
								server.loadServerSettingsFile("variable-terminal-server.properties");
								VTConsole.print("\rVT>Loaded settings file:[variable-terminal-server.properties]\nVT>");
								ok = true;
							}
							catch (Throwable t)
							{
								VTConsole.print("\rVT>Failed to load settings file:[variable-terminal-server.properties]\nVT>");
							}
							if (ok)
							{
								VTServerConnector connector = server.getServerConnector();
								synchronized (connector)
								{
									connector.interruptConnector();
									connector.notify();
								}
							}
						}
						else if (parsed.length >= 3)
						{
							boolean ok = false;
							try
							{
								server.loadServerSettingsFile(parsed[2]);
								VTConsole.print("\rVT>Loaded settings file:[" + parsed[2] + "]\nVT>");
								ok = true;
							}
							catch (Throwable t)
							{
								VTConsole.print("\rVT>Failed to load settings file:[" + parsed[2] + "]\nVT>");
							}
							if (ok)
							{
								VTServerConnector connector = server.getServerConnector();
								synchronized (connector)
								{
									connector.interruptConnector();
									connector.notify();
								}
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("SL"))
					{
						if (parsed.length == 2)
						{
							int sessionsLimit = server.getServerConnector().getSessionsLimit();
							VTConsole.print("\rVT>Sessions limit(SL): [" + sessionsLimit + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							int sessionsLimit = Integer.parseInt(parsed[2]);
							if (sessionsLimit < 0)
							{
								sessionsLimit = 0;
							}
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setSessionsLimit(sessionsLimit);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Sessions limit(SL) set to: [" + sessionsLimit + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("CM"))
					{
						if (parsed.length == 2)
						{
							if (server.getServerConnector().isPassive())
							{
								VTConsole.print("\rVT>Connection mode(CM): [Passive]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Connection mode(CM): [Active]\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							boolean passive = !parsed[2].toUpperCase().startsWith("A");
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setPassive(passive);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Connection mode(CM) set to: [" + (passive ? "Passive" : "Active") + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("CH"))
					{
						if (parsed.length == 2)
						{
							String hostAddress = server.getServerConnector().getAddress();
							VTConsole.print("\rVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							String hostAddress = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setAddress(hostAddress);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Connection host address(CH) set to: [" + hostAddress + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("CP"))
					{
						if (parsed.length == 2)
						{
							Integer port = server.getServerConnector().getPort();
							VTConsole.print("\rVT>Connection host port(CP): [" + port + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							try
							{
								int port = Integer.parseInt(parsed[2]);
								if (port < 1 || port > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
								}
								else
								{
									VTServerConnector connector = server.getServerConnector();
									synchronized (connector)
									{
										connector.setPort(port);
										connector.interruptConnector();
										connector.notify();
									}
									VTConsole.print("\rVT>Connection host port(CP) set to: [" + port + "]\nVT>");
								}
							}
							catch (NumberFormatException e)
							{
								VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PT"))
					{
						if (parsed.length == 2)
						{
							String proxyType = server.getServerConnector().getProxyType();
							if (proxyType == null)
							{
								VTConsole.print("\rVT>Proxy type(PT): [None]\nVT>");
							}
							else if (proxyType.toUpperCase().startsWith("H"))
							{
								VTConsole.print("\rVT>Proxy type(PT): [HTTP]\nVT>");
							}
							else if (proxyType.toUpperCase().startsWith("S"))
							{
								VTConsole.print("\rVT>Proxy type(PT): [SOCKS]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Proxy type(PT): [None]\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							String proxyType = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setProxyType(proxyType);
								connector.interruptConnector();
								connector.notify();
							}
							if (proxyType.toUpperCase().startsWith("H"))
							{
								VTConsole.print("\rVT>Proxy type(PT) set to: [HTTP]\nVT>");
							}
							else if (proxyType.toUpperCase().startsWith("S"))
							{
								VTConsole.print("\rVT>Proxy type(PT) set to: [SOCKS]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Proxy type(PT) set to: [None]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PH"))
					{
						if (parsed.length == 2)
						{
							String proxyAddress = server.getServerConnector().getProxyAddress();
							VTConsole.print("\rVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							String proxyAddress = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setProxyAddress(proxyAddress);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Proxy host address set to: [" + proxyAddress + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PP"))
					{
						if (parsed.length == 2)
						{
							Integer proxyPort = server.getServerConnector().getProxyPort();
							if (proxyPort != null)
							{
								VTConsole.print("\rVT>Proxy host port(PP): [" + proxyPort + "]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Proxy host port(PP): []\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							try
							{
								int proxyPort = Integer.parseInt(parsed[2]);
								if (proxyPort < 1 || proxyPort > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
								}
								else
								{
									VTServerConnector connector = server.getServerConnector();
									synchronized (connector)
									{
										connector.setProxyPort(proxyPort);
										connector.interruptConnector();
										connector.notify();
									}
									VTConsole.print("\rVT>Proxy host port(PP) set to: [" + proxyPort + "]\nVT>");
								}
							}
							catch (NumberFormatException e)
							{
								VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PA"))
					{
						if (parsed.length == 2)
						{
							if (!server.getServerConnector().isUseProxyAuthentication())
							{
								VTConsole.print("\rVT>Proxy authentication(PA): [Disabled]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Proxy authentication(PA): [Enabled]\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							if (parsed[2].toUpperCase().startsWith("E"))
							{
								VTServerConnector connector = server.getServerConnector();
								synchronized (connector)
								{
									connector.setUseProxyAuthentication(true);
									connector.interruptConnector();
									connector.notify();
								}
								VTConsole.print("\rVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
							}
							else
							{
								VTServerConnector connector = server.getServerConnector();
								synchronized (connector)
								{
									connector.setUseProxyAuthentication(false);
									connector.interruptConnector();
									connector.notify();
								}
								VTConsole.print("\rVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PU"))
					{
						if (parsed.length == 2)
						{
							String proxyUser = server.getServerConnector().getProxyUser();
							VTConsole.print("\rVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							String proxyUser = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setProxyUser(proxyUser);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Proxy user(PU) set to: [" + proxyUser + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("PS"))
					{
						if (parsed.length == 2)
						{
							String proxyPassword = server.getServerConnector().getProxyPassword();
							VTConsole.print("\rVT>Proxy password(PS): [" + proxyPassword + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							String proxyPassword = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setProxyPassword(proxyPassword);
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Proxy password(PS) set to: [" + proxyPassword + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("ET"))
					{
						if (parsed.length == 2)
						{
							String encryptionType = server.getServerConnector().getEncryptionType();
							if (encryptionType.toUpperCase().startsWith("R"))
							{
								VTConsole.print("\rVT>Encryption type(ET): [RC4]\nVT>");
							}
							else if (encryptionType.toUpperCase().startsWith("A"))
							{
								VTConsole.print("\rVT>Encryption type(ET): [AES]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Encryption type(ET): [None]\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							String encryptionType = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setEncryptionType(encryptionType);
								connector.interruptConnector();
								connector.notify();
							}
							if (encryptionType.toUpperCase().startsWith("R"))
							{
								VTConsole.print("\rVT>Encryption type(ET) set to: [RC4]\nVT>");
							}
							else if (encryptionType.toUpperCase().startsWith("A"))
							{
								VTConsole.print("\rVT>Encryption type(ET) set to: [AES]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Encryption type(ET) set to: [None]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("ES"))
					{
						if (parsed.length == 2)
						{
							String encryptionPassword = "";
							if (server.getServerConnector().getEncryptionKey() != null)
							{
								encryptionPassword = new String(server.getServerConnector().getEncryptionKey(), "UTF-8");
							}
							VTConsole.print("\rVT>Encryption password(ES): [" + encryptionPassword + "]\nVT>");
						}
						else if (parsed.length >= 3)
						{
							String encryptionPassword = parsed[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setEncryptionKey(encryptionPassword.getBytes("UTF-8"));
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Encryption password(ES) set to: [" + encryptionPassword + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else if (parsed[1].equalsIgnoreCase("NP"))
					{
						if (parsed.length == 2)
						{
							Integer natPort = server.getServerConnector().getNatPort();
							if (natPort != null)
							{
								VTConsole.print("\rVT>Connection nat port(NP): [" + natPort + "]\nVT>");
							}
							else
							{
								VTConsole.print("\rVT>Connection nat port(NP): []\nVT>");
							}
						}
						else if (parsed.length >= 3)
						{
							try
							{
								int natPort = Integer.parseInt(parsed[2]);
								if (natPort < 1 || natPort > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
								}
								else
								{
									VTServerConnector connector = server.getServerConnector();
									if (natPort == 0)
									{
										synchronized (connector)
										{
											connector.setNatPort(null);
										}
										VTConsole.print("\rVT>Connection nat port(NP) set to: []\nVT>");
									}
									else
									{
										synchronized (connector)
										{
											connector.setNatPort(natPort);
										}
										VTConsole.print("\rVT>Connection nat port(NP) set to: [" + natPort + "]\nVT>");
									}
								}
							}
							catch (NumberFormatException e)
							{
								VTServerConnector connector = server.getServerConnector();
								synchronized (connector)
								{
									connector.setNatPort(null);
								}
								VTConsole.print("\rVT>Connection nat port(NP) set to: []\nVT>");
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
						}
					}
					else
					{
						VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
					}
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTTIME") || parsed[0].equalsIgnoreCase("*VTTM"))
			{
				clock.setTime(Calendar.getInstance().getTime());
				VTConsole.print("\rVT>Date/time ([ER-Y-MM-DD][HH:MM:SS:MS-TZ]) on server:\nVT>[" + firstDateTimeFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondDateTimeFormat.format(clock.getTime()) + "\nVT>");
			}
			else if (parsed[0].equalsIgnoreCase("*VTLOCK") || parsed[0].equalsIgnoreCase("*VTLK"))
			{
				if (parsed.length >= 3)
				{
					// int separator = splitCommand[1].indexOf('/');
					// if (separator >= 0)
					// {
					// String login = splitCommand[1].substring(0, separator);
					// String password = splitCommand[1].substring(separator +
					// 1);
					// server.setUniqueUserCredential(login, password);
					// VTConsole.print("\rVT>Single credential set!\nVT>");
					// }
					// else
					// {
					// VTConsole.print("\rVT>Invalid command syntax!" +
					// VTHelpManager.getHelpForServerCommand(splitCommand[0]));
					// }
					String login = parsed[1];
					String password = parsed[2];
					server.setUniqueUserCredential(login, password);
					VTConsole.print("\rVT>Single credential set!\nVT>");
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
				}
			}
			else if (parsed[0].equalsIgnoreCase("*VTMIXERS") || parsed[0].equalsIgnoreCase("*VTMX"))
			{
				message.setLength(0);
				message.append("\nVT>List of local audio mixers:\nVT>");
				Mixer.Info[] mixers = AudioSystem.getMixerInfo();
				int number = 0;
				for (Mixer.Info info : mixers)
				{
					// Mixer mixer = AudioSystem.getMixer(info);
					message.append("\nVT>Number: [" + number++ + "]");
					message.append("\nVT>Name: [" + info.getName() + "]");
					message.append("\nVT>Description: [" + info.getDescription() + "]");
					// for (Line.Info line : mixer.getSourceLineInfo())
					// {
					// message.append("\nVT>Line: [" + line.toString() + "]");
					// }
					// for (Line.Info line : mixer.getTargetLineInfo())
					// {
					// message.append("\nVT>Line: [" + line.toString() + "]");
					// }
					message.append("\nVT>");
				}
				message.append("\nVT>End of local audio mixers list\nVT>");
				VTConsole.print(message.toString());
			}
			else if (parsed[0].equalsIgnoreCase("*VTPING") || parsed[0].equalsIgnoreCase("*VTPG"))
			{
				int i = 0;
				message.setLength(0);
				List<VTServerConnectionHandler> connections = server.getServerConnector().getConnectionHandlers();
				synchronized (connections)
				{
					if (connections.size() > 0)
					{
						message.append("\rVT>List of current client connection latencies on server:\nVT>");
						for (VTServerConnectionHandler handler : connections)
						{
							message.append("\nVT>Number: [" + i++ + "]");
							//message.append("\nVT>Authenticated: [" + (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
							//message.append("\nVT>Login: [" + (handler.getSessionHandler().getLogin() != null ? handler.getSessionHandler().getLogin() : "") + "]");
							InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
							
							if (address != null)
							{
								long millisseconds = 0;
								long nanosseconds = 0;
								String hostAddress = "";
								try
								{
									hostAddress = address.getHostAddress();
									VTServerSession session = handler.getSessionHandler().getSession();
									long clientTime = session.getLocalNanoDelay();
									long serverTime = session.getRemoteNanoDelay();
									nanosseconds = ((clientTime + serverTime) / 2);
									millisseconds = ((clientTime + serverTime) / 2) / 1000000;
								}
								catch (Throwable t)
								{
									
								}
								
								message.append("\nVT>Host address: [" + hostAddress +
								"]\nVT>Estimated connection latency: [" + millisseconds + "] ms or [" + nanosseconds + 
								// "]\nVT>Host name: [" +
								// address.getHostName() +
								// "]\nVT>Canonical host name: [" +
								// address.getCanonicalHostName() +
								"] ns\nVT>");
							}
						}
						message.append("\nVT>End of current client connection latencies list\nVT>");
						VTConsole.print(message.toString());
					}
					else
					{
						VTConsole.print("\rVT>Not connected with clients!\nVT>");
					}
				}
				VTConsole.print("\nVT>");
			}
			else if (parsed[0].equalsIgnoreCase("*VTCOVER") || parsed[0].equalsIgnoreCase("*VTCV"))
			{
				if (server.isDaemon())
				{
					
				}
				else
				{
					if (VTConsole.isDaemon())
					{
						server.enableTrayIcon();
						VTConsole.print("\rVT>Server console interface enabled\nVT>");
						VTConsole.setDaemon(false);
					}
					else
					{
						server.disableTrayIcon();
						VTConsole.print("\rVT>Server console interface disabled\nVT>");
						VTConsole.setDaemon(true);
						Object waiter = VTConsole.getSynchronizationObject();
						synchronized (waiter)
						{
							while (VTConsole.isDaemon())
							{
								waiter.wait();
							}
						}
					}
				}
			}
			else
			{
				VTConsole.print("\rVT>");
			}
		}
		else
		{
			// System.out.println("bug?");
			// return;
			System.exit(0);
		}
	}
}