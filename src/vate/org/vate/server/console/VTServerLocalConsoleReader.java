package org.vate.server.console;

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

public class VTServerLocalConsoleReader extends VTTask
{
	// private String command;
	// private String[] splitCommand;
	private DateFormat firstDateTimeFormat;
	private DateFormat secondDateTimeFormat;
	private GregorianCalendar clock;
	private StringBuilder message;
	private VTServer server;
	
	public VTServerLocalConsoleReader(VTServer server)
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
				executeCommand(line, null);
			}
			catch (InterruptedException e)
			{
				// e.printStackTrace();
			}
			catch (Throwable e)
			{
				VTConsole.print("\rVT>Error while processing command!\nVT>");
				// e.printStackTrace(VTConsole.getSystemOut());
				// return;
				/* VTTerminal.setSystemErr(); VTTerminal.setSystemOut();
				 * VTTerminal.setSystemIn(); e.printStackTrace(); */
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
				executeCommand(line, stack);
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
				executeCommand(line, stack);
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
		String splitCommand[];
		if (command != null)
		{
			if (!(command.length() == 0))
			{
				splitCommand = CommandLineTokenizer.tokenize(command);
				if (splitCommand.length < 1)
				{
					splitCommand = new String[] { command };
					// p = 0;
					/* for (String part : splitCommand) { splitCommand[p++] =
					 * StringEscapeUtils.unescapeJava(part); } */
				}
			}
			else
			{
				splitCommand = new String[] { "" };
			}
			if (splitCommand[0].equalsIgnoreCase("*VTMESSAGE") || splitCommand[0].equalsIgnoreCase("*VTMSG"))
			{
				if (command.contains("\""))
				{
					if (splitCommand.length >= 2)
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
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from server: [" + splitCommand[1] + "]\nVT>");
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
					else if (splitCommand.length == 1)
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
			else if (splitCommand[0].equalsIgnoreCase("*VTTERMINATE") || splitCommand[0].equalsIgnoreCase("*VTTMNT"))
			{
				System.exit(0);
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTSESSIONS") || splitCommand[0].equalsIgnoreCase("*VTSNS"))
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
			else if (splitCommand[0].equalsIgnoreCase("*VTHELP"))
			{
				if (splitCommand.length == 1)
				{
					VTConsole.print(VTHelpManager.getMainHelpForServerCommands());
				}
				else if (splitCommand.length > 1)
				{
					VTConsole.print(VTHelpManager.getHelpForServerCommand(splitCommand[1]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTHLP"))
			{
				if (splitCommand.length == 1)
				{
					VTConsole.print(VTHelpManager.getMinHelpForServerCommands());
				}
				else if (splitCommand.length > 1)
				{
					VTConsole.print(VTHelpManager.getHelpForServerCommand(splitCommand[1]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTCLEAR") || splitCommand[0].equalsIgnoreCase("*VTCLR"))
			{
				VTConsole.clear();
				VTConsole.print("VT>");
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTNETWORKINTERFACES") || splitCommand[0].equalsIgnoreCase("*VTNTIS"))
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
						message.append("\nVT>Name: [" + networkInterface.getName() + "]\nVT>Display name: [" + networkInterface.getDisplayName() + "]");
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
			else if (splitCommand[0].equalsIgnoreCase("*VTDISPLAYDEVICES") || splitCommand[0].equalsIgnoreCase("*VTDPDS"))
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
			else if (splitCommand[0].equalsIgnoreCase("*VTFILEROOTS") || splitCommand[0].equalsIgnoreCase("*VTFRTS"))
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
			else if (splitCommand[0].equalsIgnoreCase("*VTPRINTSERVICES") || splitCommand[0].equalsIgnoreCase("*VTPSVS"))
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
			else if (splitCommand[0].equalsIgnoreCase("*VTENVIRONMENT") || splitCommand[0].equalsIgnoreCase("*VTENV"))
			{
				if (splitCommand.length == 1)
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
				else if (splitCommand.length == 2)
				{
					String value = VTNativeUtils.getvirtualenv(splitCommand[1]);
					if (value != null)
					{
						VTConsole.print("\rVT>[" + splitCommand[1] + "]=[" + value + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>Environment variable [" + splitCommand[1] + "] not found on server!\nVT>");
					}
				}
				else if (splitCommand.length == 3)
				{
					if (VTNativeUtils.putvirtualenv(splitCommand[1], splitCommand[2]) == 0)
					{
						VTConsole.print("\rVT>[" + splitCommand[1] + "]=[" + splitCommand[2] + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>Environment variable [" + splitCommand[1] + "] failed to be set to [" + splitCommand[2] + "] on server!\nVT>");
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTPROPERTY") || splitCommand[0].equalsIgnoreCase("*VTPROP"))
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand.length == 1)
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
				else if (splitCommand.length == 2)
				{
					String value = System.getProperty(splitCommand[1]);
					if (value != null)
					{
						VTConsole.print("\rVT>[" + splitCommand[1] + "]=[" + value + "]\nVT>");
					}
					else
					{
						VTConsole.print("\rVT>JVM property [" + splitCommand[1] + "] not found on server!\nVT>");
					}
				}
				else if (splitCommand.length == 3)
				{
					try
					{
						System.setProperty(splitCommand[1], splitCommand[2]);
						VTConsole.print("\rVT>[" + splitCommand[1] + "]=[" + splitCommand[2] + "]\nVT>");
					}
					catch (Throwable e)
					{
						VTConsole.print("\rVT>JVM property [" + splitCommand[1] + "] failed to be set to [" + splitCommand[2] + "] on server!\nVT>");
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTDISCONNECT") || splitCommand[0].equalsIgnoreCase("*VTDSCT"))
			{
				if (splitCommand.length == 1)
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
				else if (splitCommand.length >= 2)
				{
					try
					{
						int number = Integer.parseInt(splitCommand[1]);
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
										VTConsole.print("\rVT>Client number [" + splitCommand[1] + "] is not valid!\nVT>");
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					catch (NumberFormatException e)
					{
						VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
					}
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTSETTING") || splitCommand[0].equalsIgnoreCase("*VTSTG"))
			{
				if (splitCommand.length == 1)
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
					message.append("\nVT>Encryption password(EK): [" + encryptionPassword + "]");
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
					message.append("\nVT>Sessions limit(SL): [" + sessionsLimit + "]");
					message.append("\nVT>\nVT>End of connection settings list on server\nVT>");
					VTConsole.print(message.toString());
					message.setLength(0);
				}
				else if (splitCommand.length >= 2)
				{
					if (splitCommand[1].equalsIgnoreCase("SL"))
					{
						if (splitCommand.length == 2)
						{
							int sessionsLimit = server.getServerConnector().getSessionsLimit();
							VTConsole.print("\rVT>Sessions limit(SL): [" + sessionsLimit + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							int sessionsLimit = Integer.parseInt(splitCommand[2]);
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("CM"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							boolean passive = !splitCommand[2].toUpperCase().startsWith("A");
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("CH"))
					{
						if (splitCommand.length == 2)
						{
							String hostAddress = server.getServerConnector().getAddress();
							VTConsole.print("\rVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							String hostAddress = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("CP"))
					{
						if (splitCommand.length == 2)
						{
							Integer port = server.getServerConnector().getPort();
							VTConsole.print("\rVT>Connection host port(CP): [" + port + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							try
							{
								int port = Integer.parseInt(splitCommand[2]);
								if (port < 1 || port > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
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
								VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PT"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							String proxyType = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PH"))
					{
						if (splitCommand.length == 2)
						{
							String proxyAddress = server.getServerConnector().getProxyAddress();
							VTConsole.print("\rVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							String proxyAddress = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PP"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							try
							{
								int proxyPort = Integer.parseInt(splitCommand[2]);
								if (proxyPort < 1 || proxyPort > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
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
								VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
							}
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PA"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							if (splitCommand[2].toUpperCase().startsWith("E"))
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PU"))
					{
						if (splitCommand.length == 2)
						{
							String proxyUser = server.getServerConnector().getProxyUser();
							VTConsole.print("\rVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							String proxyUser = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("PS"))
					{
						if (splitCommand.length == 2)
						{
							String proxyPassword = server.getServerConnector().getProxyPassword();
							VTConsole.print("\rVT>Proxy password(PS): [" + proxyPassword + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							String proxyPassword = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("ET"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							String encryptionType = splitCommand[2];
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("EK"))
					{
						if (splitCommand.length == 2)
						{
							String encryptionPassword = "";
							if (server.getServerConnector().getEncryptionKey() != null)
							{
								encryptionPassword = new String(server.getServerConnector().getEncryptionKey(), "UTF-8");
							}
							VTConsole.print("\rVT>Encryption password(EK): [" + encryptionPassword + "]\nVT>");
						}
						else if (splitCommand.length >= 3)
						{
							String encryptionPassword = splitCommand[2];
							VTServerConnector connector = server.getServerConnector();
							synchronized (connector)
							{
								connector.setEncryptionKey(encryptionPassword.getBytes("UTF-8"));
								connector.interruptConnector();
								connector.notify();
							}
							VTConsole.print("\rVT>Encryption password(EK) set to: [" + encryptionPassword + "]\nVT>");
						}
						else
						{
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].equalsIgnoreCase("NP"))
					{
						if (splitCommand.length == 2)
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
						else if (splitCommand.length >= 3)
						{
							try
							{
								int natPort = Integer.parseInt(splitCommand[2]);
								if (natPort < 1 || natPort > 65535)
								{
									VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
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
							VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
					}
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTTIME") || splitCommand[0].equalsIgnoreCase("*VTTM"))
			{
				clock.setTime(Calendar.getInstance().getTime());
				VTConsole.print("\rVT>Date/time ([ER-Y-MM-DD][HH:MM:SS:MS-TZ]) on server:\nVT>[" + firstDateTimeFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondDateTimeFormat.format(clock.getTime()) + "\nVT>");
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTRESETLOCK") || splitCommand[0].equalsIgnoreCase("*VTRSL"))
			{
				if (splitCommand.length >= 3)
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
					String login = splitCommand[1];
					String password = splitCommand[2];
					server.setUniqueUserCredential(login, password);
					VTConsole.print("\rVT>Single credential set!\nVT>");
				}
				else
				{
					VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTAUDIOMIXERS") || splitCommand[0].equalsIgnoreCase("*VTAM"))
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
			else if (splitCommand[0].equalsIgnoreCase("*VTPING") || splitCommand[0].equalsIgnoreCase("*VTPG"))
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
								long estimated = 0;
								String hostAddress = "";
								try
								{
									hostAddress = address.getHostAddress();
									VTServerSession session = handler.getSessionHandler().getSession();
									long clientTime = session.getLocalNanoDelay();
									long serverTime = session.getRemoteNanoDelay();
									estimated = ((clientTime + serverTime) / 2) / 1000000;
								}
								catch (Throwable t)
								{
									
								}
								
								message.append("\nVT>Host address: [" + hostAddress +
								"]\nVT>Estimated connection latency: [" + estimated + 
								// "]\nVT>Host name: [" +
								// address.getHostName() +
								// "]\nVT>Canonical host name: [" +
								// address.getCanonicalHostName() +
								"] ms\nVT>");
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