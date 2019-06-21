package org.vate.server.console;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.filesystem.VTRootList;
import org.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vate.graphics.message.VTGraphicsMessager;
import org.vate.help.VTHelpManager;
import org.vate.nativeutils.VTNativeUtils;
import org.vate.server.connection.VTServerConnection;
import org.vate.server.connection.VTServerConnectionHandler;
import org.vate.server.connection.VTServerConnector;
import org.vate.server.filesystem.VTServerFileModifyOperation;
import org.vate.server.filesystem.VTServerFileScanOperation;
import org.vate.server.print.VTServerPrintDataTask;
import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;
import org.vate.tunnel.channel.VTTunnelChannelSocketListener;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerRemoteConsoleReader extends VTTask
{
	// private String command = "";
	// private String[] splitCommand;
	private StringBuilder message;
	private GregorianCalendar clock;
	private DateFormat firstDateTimeFormat;
	private DateFormat secondDateTimeFormat;
	private VTServerSession session;
	private VTServerConnection connection;
	
	public VTServerRemoteConsoleReader(VTServerSession session)
	{
		this.session = session;
		this.connection = session.getConnection();
		this.stopped = false;
		this.clock = new GregorianCalendar();
		this.firstDateTimeFormat = new SimpleDateFormat("G", Locale.ENGLISH);
		this.secondDateTimeFormat = new SimpleDateFormat("MM-dd][HH:mm:ss:SSS-z]");
		// this.command = "";
		this.message = new StringBuilder();
	}
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	public void run()
	{
		// int p = 0;
		while (!stopped)
		{
			try
			{
				String line = connection.getCommandReader().readLine();
				
				executeCommand(line, null);
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
		}
		synchronized (session)
		{
			session.notify();
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
		if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
		{
			if (splitCommand.length == 2)
			{
				if (splitCommand[1].toUpperCase().startsWith("S"))
				{
					session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(true);
				}
				else
				{
					
				}
			}
			else if (splitCommand.length >= 4)
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand[1].toUpperCase().contains("G"))
				{
					if (session.getFileTransferServer().aliveThread())
					{
						session.getFileTransferServer().joinThread();
					}
					session.getFileTransferServer().getHandler().getSession().getTransaction().setFinished(false);
					session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(false);
					session.getFileTransferServer().getHandler().getSession().getTransaction().setCommand(command);
					session.getFileTransferServer().startThread();
				}
				else if (splitCommand[1].toUpperCase().contains("P"))
				{
					if (session.getFileTransferServer().aliveThread())
					{
						session.getFileTransferServer().joinThread();
					}
					session.getFileTransferServer().getHandler().getSession().getTransaction().setFinished(false);
					session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(false);
					session.getFileTransferServer().getHandler().getSession().getTransaction().setCommand(command);
					session.getFileTransferServer().startThread();
				}
				else
				{
					
				}
			}
			else
			{
				
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTSCREENSHOT") || splitCommand[0].equalsIgnoreCase("*VTSCS"))
		{
			synchronized (session.getScreenshotTask())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand.length == 1)
				{
					if (session.getScreenshotTask().isFinished())
					{
						session.getScreenshotTask().joinThread();
					}
					if (!session.getScreenshotTask().aliveThread())
					{
						session.getScreenshotTask().setFinished(false);
						session.getScreenshotTask().setDrawPointer(false);
						session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
						session.getScreenshotTask().setDeviceNumber(-1);
						session.getScreenshotTask().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another screen capture is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length >= 2)
				{
					if (session.getScreenshotTask().isFinished())
					{
						session.getScreenshotTask().joinThread();
					}
					if (!session.getScreenshotTask().aliveThread())
					{
						session.getScreenshotTask().setFinished(false);
						session.getScreenshotTask().setDrawPointer(false);
						session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
						session.getScreenshotTask().setDeviceNumber(-1);
						for (int i = 1; i < splitCommand.length; i++)
						{
							try
							{
								session.getScreenshotTask().setDeviceNumber(Integer.parseInt(splitCommand[i]));
							}
							catch (Throwable t)
							{
								
							}
							if (splitCommand[i].toUpperCase().contains("S"))
							{
								session.getScreenshotTask().setDrawPointer(true);
							}
							if (splitCommand[i].toUpperCase().contains("C"))
							{
								session.getScreenshotTask().setDrawPointer(false);
							}
							if (splitCommand[i].toUpperCase().contains("B"))
							{
								session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
							}
							if (splitCommand[i].toUpperCase().contains("H"))
							{
								session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_HIGH);
							}
							if (splitCommand[i].toUpperCase().contains("M"))
							{
								session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_MEDIUM);
							}
							if (splitCommand[i].toUpperCase().contains("L"))
							{
								session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_LOW);
							}
							if (splitCommand[i].toUpperCase().contains("W"))
							{
								session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_WORST);
							}
						}
						session.getScreenshotTask().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another screen capture is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTMESSAGE") || splitCommand[0].equalsIgnoreCase("*VTMSG"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			// System.out.println(command);
			if (command.contains("\""))
			{
				if (splitCommand.length >= 2)
				{
					List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								if (connectionHandler.getConnection() != connection)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + splitCommand[1] + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												
											}
										}
									}
								}
							}
						}
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[" + splitCommand[1] + "]");
						VTConsole.print("\u0007\rVT>Message from client: [" + splitCommand[1] + "]\nVT>");
						// VTConsole.bell();
						connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 1)
				{
					List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								if (connectionHandler.getConnection() != connection)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: []\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												
											}
										}
									}
								}
							}
						}
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[]");
						VTConsole.print("\u0007\rVT>Message from client: []\nVT>");
						// VTConsole.bell();
						connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
			else
			{
				if (command.length() >= 12 && command.charAt(11) == ' ')
				{
					// command = StringEscapeUtils.unescapeJava(command);
					List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								if (connectionHandler.getConnection() != connection)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + command.substring(12) + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												
											}
										}
									}
								}
							}
						}
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[" + command.substring(12) + "]");
						VTConsole.print("\u0007\rVT>Message from client: [" + command.substring(12) + "]\nVT>");
						// VTConsole.bell();
						connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else if (command.length() >= 8 && command.charAt(7) == ' ')
				{
					// command = StringEscapeUtils.unescapeJava(command);
					List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								if (connectionHandler.getConnection() != connection)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: [" + command.substring(12) + "]\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												
											}
										}
									}
								}
							}
						}
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[" + command.substring(8) + "]");
						VTConsole.print("\u0007\rVT>Message from client: [" + command.substring(8) + "]\nVT>");
						// VTConsole.bell();
						connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					List<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
					synchronized (connections)
					{
						if (connections.size() > 0)
						{
							for (VTServerConnectionHandler connectionHandler : connections)
							{
								if (connectionHandler.getConnection() != connection)
								{
									if (connectionHandler.getSessionHandler().isAuthenticated())
									{
										if (connectionHandler.getConnection() != null && connectionHandler.getConnection().isConnected())
										{
											try
											{
												connectionHandler.getConnection().getResultWriter().write("\u0007\nVT>Message from client: []\nVT>");
												connectionHandler.getConnection().getResultWriter().flush();
											}
											catch (Throwable e)
											{
												
											}
										}
									}
								}
							}
						}
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[]");
						VTConsole.print("\u0007\rVT>Message from client: []\nVT>");
						// VTConsole.bell();
						connection.getResultWriter().write("\nVT>Message received by server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTDISCONNECT") || splitCommand[0].equalsIgnoreCase("*VTDSCT"))
		{
			VTConsole.print("\rVT>Client disconnecting...\nVT>");
			connection.closeSockets();
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTTERMINATE") || splitCommand[0].equalsIgnoreCase("*VTTMNT"))
		{
			VTConsole.print("\rVT>Client finalizing server...\nVT>");
			connection.closeSockets();
			System.exit(0);
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTRUNTIME") || splitCommand[0].equalsIgnoreCase("*VTRT"))
		{
			synchronized (session.getRuntimeExecutor())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand.length > 1)
				{
					if (session.getRuntimeExecutor().isFinished())
					{
						session.getRuntimeExecutor().joinThread();
					}
					if (!session.getRuntimeExecutor().aliveThread())
					{
						session.getRuntimeExecutor().setFinished(false);
						session.getRuntimeExecutor().setCommand(command);
						session.getRuntimeExecutor().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another runtime execution is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTGRAPHICSLINK") || splitCommand[0].equalsIgnoreCase("*VTGL"))
		{
			if (splitCommand.length >= 2)
			{
				/* if (splitCommand[1].toUpperCase().startsWith("S")) {
				 * session.getGraphicsServer().setStopped(true);
				 * session.getGraphicsThread().join(); } else */
				if (splitCommand[1].toUpperCase().startsWith("V"))
				{
					// connection.getResultWriter().write(command);
					// connection.getResultWriter().flush();
					session.getGraphicsServer().joinThread();
					session.getGraphicsServer().setReadOnly(true);
					session.getGraphicsServer().startThread();
				}
				else if (splitCommand[1].toUpperCase().startsWith("C"))
				{
					// connection.getResultWriter().write(command);
					// connection.getResultWriter().flush();
					session.getGraphicsServer().joinThread();
					session.getGraphicsServer().setReadOnly(false);
					session.getGraphicsServer().startThread();
				}
				else
				{
					
				}
			}
			else if (splitCommand.length == 1)
			{
				if (session.getGraphicsServer().aliveThread())
				{
					session.getGraphicsServer().setStopped(true);
					session.getGraphicsServer().joinThread();
				}
				else
				{
					session.getGraphicsServer().joinThread();
					session.getGraphicsServer().setReadOnly(false);
					session.getGraphicsServer().startThread();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTHELP"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			// connection.getResultWriter().write(vtHelpMessage);
			if (splitCommand.length == 1)
			{
				connection.getResultWriter().write(VTHelpManager.getMainHelpForClientCommands());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length > 1)
			{
				connection.getResultWriter().write(VTHelpManager.getHelpForClientCommand(splitCommand[1]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTHLP"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			// connection.getResultWriter().write(vtHelpMessage);
			if (splitCommand.length == 1)
			{
				connection.getResultWriter().write(VTHelpManager.getMinHelpForClientCommands());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length > 1)
			{
				connection.getResultWriter().write(VTHelpManager.getHelpForClientCommand(splitCommand[1]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTBEEP") || splitCommand[0].equalsIgnoreCase("*VTBP"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (splitCommand.length >= 3)
			{
				try
				{
					if (VTNativeUtils.beep(Integer.parseInt(splitCommand[1]), Integer.parseInt(splitCommand[2]), false))
					{
						connection.getResultWriter().write("\nVT>Beep is playing on server!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Beep is not playing on server!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (NumberFormatException e)
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
				catch (Throwable e)
				{
					
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTVARIABLE") || splitCommand[0].equalsIgnoreCase("*VTVAR"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (splitCommand.length == 1)
			{
				message.setLength(0);
				message.append("\nVT>List of environment variables on server:\nVT>");
				for (Entry<String, String> variable : VTNativeUtils.getvirtualenv().entrySet())
				{
					message.append("\nVT>[" + variable.getKey() + "]=[" + variable.getValue() + "]");
				}
				message.append("\nVT>\nVT>End of environment variables list\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length == 2)
			{
				String value = VTNativeUtils.getvirtualenv(splitCommand[1]);
				if (value != null)
				{
					connection.getResultWriter().write("\nVT>[" + splitCommand[1] + "]=[" + value + "]\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Environment variable [" + splitCommand[1] + "] not found on server!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else if (splitCommand.length >= 3)
			{
				if (VTNativeUtils.putvirtualenv(splitCommand[1], splitCommand[2]) == 0)
				{
					connection.getResultWriter().write("\nVT>[" + splitCommand[1] + "]=[" + splitCommand[2] + "]\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Environment variable [" + splitCommand[1] + "] failed to be set to [" + splitCommand[2] + "] on server!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTPROPERTY") || splitCommand[0].equalsIgnoreCase("*VTPROP"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (splitCommand.length == 1)
			{
				message.setLength(0);
				message.append("\nVT>List of JVM properties on server:\nVT>");
				for (Entry<Object, Object> property : System.getProperties().entrySet())
				{
					message.append("\nVT>[" + property.getKey().toString() + "]=[" + property.getValue().toString() + "]");
				}
				message.append("\nVT>\nVT>End of JVM properties list\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length == 2)
			{
				String value = System.getProperty(splitCommand[1]);
				if (value != null)
				{
					connection.getResultWriter().write("\nVT>[" + splitCommand[1] + "]=[" + value + "]\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>JVM property [" + splitCommand[1] + "] not found on server!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else if (splitCommand.length >= 3)
			{
				try
				{
					System.setProperty(splitCommand[1], splitCommand[2]);
					connection.getResultWriter().write("\nVT>[" + splitCommand[1] + "]=[" + splitCommand[2] + "]\nVT>");
					connection.getResultWriter().flush();
				}
				catch (Throwable e)
				{
					connection.getResultWriter().write("\nVT>JVM property [" + splitCommand[1] + "] failed to be set to [" + splitCommand[2] + "] on server!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTTIME") || splitCommand[0].equalsIgnoreCase("*VTTM"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			clock.setTime(Calendar.getInstance().getTime());
			connection.getResultWriter().write("\nVT>Date/time ([ER-Y-MM-DD][HH:MM:SS:MS-TZ]) on server:\nVT>[" + firstDateTimeFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondDateTimeFormat.format(clock.getTime()) + "\nVT>");
			connection.getResultWriter().flush();
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTRESOLVEHOST") || splitCommand[0].equalsIgnoreCase("*VTRHT"))
		{
			synchronized (session.getHostResolver())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand.length >= 2)
				{
					if (session.getHostResolver().isFinished())
					{
						session.getHostResolver().joinThread();
					}
					if (!session.getHostResolver().aliveThread())
					{
						session.getHostResolver().setFinished(false);
						session.getHostResolver().setHost(splitCommand[1]);
						session.getHostResolver().startThread();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another network host resolution is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTNETWORKINTERFACES") || splitCommand[0].equalsIgnoreCase("*VTNTIS"))
		{
			synchronized (session.getNetworkInterfaceResolver())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (session.getNetworkInterfaceResolver().isFinished())
				{
					session.getNetworkInterfaceResolver().joinThread();
				}
				if (!session.getNetworkInterfaceResolver().aliveThread())
				{
					session.getNetworkInterfaceResolver().setFinished(false);
					session.getNetworkInterfaceResolver().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another network interface search is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTDISPLAYDEVICES") || splitCommand[0].equalsIgnoreCase("*VTDPDS"))
		{
			synchronized (session.getGraphicsDeviceResolver())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (session.getGraphicsDeviceResolver().isFinished())
				{
					session.getGraphicsDeviceResolver().joinThread();
				}
				if (!session.getGraphicsDeviceResolver().aliveThread())
				{
					session.getGraphicsDeviceResolver().setFinished(false);
					session.getGraphicsDeviceResolver().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another graphical display device search is still running\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTFILEROOTS") || splitCommand[0].equalsIgnoreCase("*VTFRTS"))
		{
			synchronized (session.getFileSystemRootsResolver())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (session.getFileSystemRootsResolver().isFinished())
				{
					session.getFileSystemRootsResolver().joinThread();
				}
				if (!session.getFileSystemRootsResolver().aliveThread())
				{
					session.getFileSystemRootsResolver().setFinished(false);
					session.getFileSystemRootsResolver().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another file system roots resolution is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTOPTICALDRIVE") || splitCommand[0].equalsIgnoreCase("*VTOPDR"))
		{
			synchronized (session.getCDOperation())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (splitCommand.length >= 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("O"))
					{
						if (session.getCDOperation().isFinished())
						{
							session.getCDOperation().joinThread();
						}
						if (!session.getCDOperation().aliveThread())
						{
							session.getCDOperation().setFinished(false);
							session.getCDOperation().setOpen(true);
							session.getCDOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("C"))
					{
						if (session.getCDOperation().isFinished())
						{
							session.getCDOperation().joinThread();
						}
						if (!session.getCDOperation().aliveThread())
						{
							session.getCDOperation().setFinished(false);
							session.getCDOperation().setOpen(false);
							session.getCDOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTSESSIONS") || splitCommand[0].equalsIgnoreCase("*VTSNS"))
		{
			synchronized (session.getConnectionListViewer())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (session.getConnectionListViewer().isFinished())
				{
					session.getConnectionListViewer().joinThread();
				}
				if (!session.getConnectionListViewer().aliveThread())
				{
					session.getConnectionListViewer().setFinished(false);
					session.getConnectionListViewer().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another server connection list view is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTQUIT"))
		{
			VTConsole.print("\rVT>Client finalizing...\nVT>");
			connection.closeSockets();
		}
//		else if (splitCommand[0].equalsIgnoreCase("*VTRESTARTSHELL") || splitCommand[0].equalsIgnoreCase("*VTRTSH"))
//		{
//			// connection.getResultWriter().write(command);
//			// connection.getResultWriter().flush();
//			connection.getResultWriter().write("\nVT>Restarting external shell...\nVT>");
//			connection.getResultWriter().flush();
//			session.setRestartingShell(true);
//			session.restartShell();
//		}
		else if (splitCommand[0].equalsIgnoreCase("*VTPRINTERS") || splitCommand[0].equalsIgnoreCase("*VTPRTS"))
		{
			synchronized (session.getPrintServiceResolver())
			{
				// connection.getResultWriter().write(command);
				// connection.getResultWriter().flush();
				if (session.getPrintServiceResolver().isFinished())
				{
					session.getPrintServiceResolver().joinThread();
				}
				if (!session.getPrintServiceResolver().aliveThread())
				{
					if (splitCommand.length >= 2)
					{
						try
						{
							int order = Integer.parseInt(splitCommand[1]);
							session.getPrintServiceResolver().setOrder(order);
						}
						catch (Throwable t)
						{
							connection.getResultWriter().write("\nVT>Print service order number [" + splitCommand[1] + "] is invalid!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						session.getPrintServiceResolver().setOrder(-1);
					}
					session.getPrintServiceResolver().setFinished(false);
					session.getPrintServiceResolver().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another print service search is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
//		else if (splitCommand[0].equalsIgnoreCase("*VTPRINTTEXT") || splitCommand[0].equalsIgnoreCase("*VTPRTX"))
//		{
//			// splitCommand[1] =
//			// StringEscapeUtils.unescapeJava(splitCommand[1]);
//			try
//			{
//				if (splitCommand.length == 2)
//				{
//					synchronized (session.getPrintTextTask())
//					{
//						// connection.getResultWriter().write(command);
//						// connection.getResultWriter().flush();
//						if (session.getPrintTextTask().isFinished())
//						{
//							session.getPrintTextTask().joinThread();
//						}
//						if (!session.getPrintTextTask().aliveThread())
//						{
//							session.getPrintTextTask().setFinished(false);
//							session.getPrintTextTask().setText(splitCommand[1]);
//							session.getPrintTextTask().setPrintServiceNumber(null);
//							connection.getResultWriter().write("\nVT>Text to print: [" + splitCommand[1] + "], print service: [Default]\nVT>");
//							connection.getResultWriter().flush();
//							session.getPrintTextTask().startThread();
//						}
//						else
//						{
//							connection.getResultWriter().write("\nVT>Another print text task is still running!\nVT>");
//							connection.getResultWriter().flush();
//						}
//					}
//				}
//				else if (splitCommand.length >= 3)
//				{
//					synchronized (session.getPrintTextTask())
//					{
//						// connection.getResultWriter().write(command);
//						// connection.getResultWriter().flush();
//						if (session.getPrintTextTask().isFinished())
//						{
//							session.getPrintTextTask().joinThread();
//						}
//						if (!session.getPrintTextTask().aliveThread())
//						{
//							session.getPrintTextTask().setFinished(false);
//							session.getPrintTextTask().setText(splitCommand[1]);
//							session.getPrintTextTask().setPrintServiceNumber(Integer.parseInt(splitCommand[2]));
//							connection.getResultWriter().write("\nVT>Text to print: [" + splitCommand[1] + "], print service: [" + splitCommand[2] + "]\nVT>");
//							connection.getResultWriter().flush();
//							session.getPrintTextTask().startThread();
//						}
//						else
//						{
//							connection.getResultWriter().write("\nVT>Another print text task is still running!\nVT>");
//							connection.getResultWriter().flush();
//						}
//					}
//				}
//				else
//				{
//					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
//					connection.getResultWriter().flush();
//				}
//			}
//			catch (NumberFormatException e)
//			{
//				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
//				connection.getResultWriter().flush();
//			}
//		}
//		else if (splitCommand[0].equalsIgnoreCase("*VTPRINTFILE") || splitCommand[0].equalsIgnoreCase("*VTPRFL"))
//		{
//			try
//			{
//				if (splitCommand.length == 2)
//				{
//					synchronized (session.getPrintFileTask())
//					{
//						// connection.getResultWriter().write(command);
//						// connection.getResultWriter().flush();
//						if (session.getPrintFileTask().isFinished())
//						{
//							session.getPrintFileTask().joinThread();
//						}
//						if (!session.getPrintFileTask().aliveThread())
//						{
//							session.getPrintFileTask().setFinished(false);
//							session.getPrintFileTask().setFile(splitCommand[1]);
//							session.getPrintFileTask().setPrintServiceNumber(null);
//							connection.getResultWriter().write("\nVT>File to print: [" + splitCommand[1] + "], print service: [Default]\nVT>");
//							connection.getResultWriter().flush();
//							session.getPrintFileTask().startThread();
//						}
//						else
//						{
//							connection.getResultWriter().write("\nVT>Another print file task is still running!\nVT>");
//							connection.getResultWriter().flush();
//						}
//					}
//				}
//				else if (splitCommand.length >= 3)
//				{
//					synchronized (session.getPrintFileTask())
//					{
//						// connection.getResultWriter().write(command);
//						// connection.getResultWriter().flush();
//						if (session.getPrintFileTask().isFinished())
//						{
//							session.getPrintFileTask().joinThread();
//						}
//						if (!session.getPrintFileTask().aliveThread())
//						{
//							session.getPrintFileTask().setFinished(false);
//							session.getPrintFileTask().setFile(splitCommand[1]);
//							session.getPrintFileTask().setPrintServiceNumber(Integer.parseInt(splitCommand[2]));
//							connection.getResultWriter().write("\nVT>File to print: [" + splitCommand[1] + "], print service: [" + splitCommand[2] + "]\nVT>");
//							connection.getResultWriter().flush();
//							session.getPrintFileTask().startThread();
//						}
//						else
//						{
//							connection.getResultWriter().write("\nVT>Another print file task is still running!\nVT>");
//							connection.getResultWriter().flush();
//						}
//					}
//				}
//				else
//				{
//					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
//					connection.getResultWriter().flush();
//				}
//			}
//			catch (NumberFormatException e)
//			{
//				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
//				connection.getResultWriter().flush();
//			}
//		}
		else if (splitCommand[0].equalsIgnoreCase("*VTPRINTDATA") || splitCommand[0].equalsIgnoreCase("*VTPRDT"))
		{
			try
			{
				if (splitCommand.length == 1)
				{
					if (session.getPrintDataTask().isFinished())
					{
						session.getPrintDataTask().joinThread();
					}
					if (!session.getPrintDataTask().aliveThread())
					{
						connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("S"))
					{
						if (session.getPrintDataTask().isFinished())
						{
							session.getPrintDataTask().joinThread();
						}
						if (!session.getPrintDataTask().aliveThread())
						{
							connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
							connection.getResultWriter().flush();
							session.getPrintDataTask().setStopped(true);
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 3)
				{
					synchronized (session.getPrintDataTask())
					{
						// connection.getResultWriter().write(command);
						// connection.getResultWriter().flush();
						if (session.getPrintDataTask().isFinished())
						{
							session.getPrintDataTask().joinThread();
						}
						if (!session.getPrintDataTask().aliveThread())
						{
							if (splitCommand[1].toUpperCase().startsWith("S"))
							{
								connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
								connection.getResultWriter().flush();
							}
							else
							{
								if (splitCommand[1].toUpperCase().startsWith("T")
								|| splitCommand[1].toUpperCase().startsWith("F")
								|| splitCommand[1].toUpperCase().startsWith("U")
								|| splitCommand[1].toUpperCase().startsWith("N"))
								{
									session.getPrintDataTask().setFinished(false);
									session.getPrintDataTask().setData(splitCommand[2]);
									session.getPrintDataTask().setPrintServiceNumber(null);
									if (splitCommand[1].toUpperCase().startsWith("T"))
									{
										session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
									}
									else
									{
										session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
									}
									if (splitCommand[1].toUpperCase().startsWith("F"))
									{
										session.getPrintDataTask().setFileEncoding("F");
									}
									if (splitCommand[1].toUpperCase().startsWith("U"))
									{
										session.getPrintDataTask().setFileEncoding("U");
									}
									if (splitCommand[1].toUpperCase().startsWith("N"))
									{
										session.getPrintDataTask().setFileEncoding("N");
									}
									connection.getResultWriter().write("\nVT>Print mode: [" + splitCommand[1] + "], data: [" + splitCommand[2] + "], service: [Default]\nVT>");
									connection.getResultWriter().flush();
									session.getPrintDataTask().startThread();
								}
								else
								{
									connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
									connection.getResultWriter().flush();
								}
							}
						}
						else
						{
							if (splitCommand[1].toUpperCase().startsWith("S"))
							{
								connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
								connection.getResultWriter().flush();
								session.getPrintDataTask().setStopped(true);
							}
							else
							{
								connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
								connection.getResultWriter().flush();
							}
						}
					}
				}
				else if (splitCommand.length >= 4)
				{
					synchronized (session.getPrintDataTask())
					{
						// connection.getResultWriter().write(command);
						// connection.getResultWriter().flush();
						if (session.getPrintDataTask().isFinished())
						{
							session.getPrintDataTask().joinThread();
						}
						if (!session.getPrintDataTask().aliveThread())
						{
							if (splitCommand[1].toUpperCase().startsWith("S"))
							{
								connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
								connection.getResultWriter().flush();
							}
							else
							{
								if (splitCommand[1].toUpperCase().startsWith("T")
								|| splitCommand[1].toUpperCase().startsWith("F")
								|| splitCommand[1].toUpperCase().startsWith("U")
								|| splitCommand[1].toUpperCase().startsWith("N"))
								{
									session.getPrintDataTask().setFinished(false);
									session.getPrintDataTask().setData(splitCommand[2]);
									session.getPrintDataTask().setPrintServiceNumber(Integer.parseInt(splitCommand[3]));
									if (splitCommand[1].toUpperCase().startsWith("T"))
									{
										session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
									}
									else
									{
										session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
									}
									if (splitCommand[1].toUpperCase().startsWith("F"))
									{
										session.getPrintDataTask().setFileEncoding("F");
									}
									if (splitCommand[1].toUpperCase().startsWith("U"))
									{
										session.getPrintDataTask().setFileEncoding("U");
									}
									if (splitCommand[1].toUpperCase().startsWith("N"))
									{
										session.getPrintDataTask().setFileEncoding("N");
									}
									connection.getResultWriter().write("\nVT>Print mode: [" + splitCommand[1] + "], data: [" + splitCommand[2] + "], service: [" + splitCommand[3] + "]\nVT>");
									connection.getResultWriter().flush();
									session.getPrintDataTask().startThread();
								}
								else
								{
									connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
									connection.getResultWriter().flush();
								}
							}
						}
						else
						{
							if (splitCommand[1].toUpperCase().startsWith("S"))
							{
								connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
								connection.getResultWriter().flush();
								session.getPrintDataTask().setStopped(true);
							}
							else
							{
								connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
								connection.getResultWriter().flush();
							}
						}
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
			catch (NumberFormatException e)
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTFILEINSPECT") || splitCommand[0].equalsIgnoreCase("*VTFI"))
		{
			synchronized (session.getFileScanOperation())
			{
				if (splitCommand.length >= 3)
				{
					if (splitCommand[1].toUpperCase().startsWith("I"))
					{
						if (session.getFileScanOperation().isFinished())
						{
							session.getFileScanOperation().joinThread();
						}
						if (!session.getFileScanOperation().aliveThread())
						{
							session.getFileScanOperation().setFinished(false);
							session.getFileScanOperation().setTarget(new File(splitCommand[2]));
							session.getFileScanOperation().setOperation(VTServerFileScanOperation.INFO_FILE);
							session.getFileScanOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file inspection is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						if (session.getFileScanOperation().isFinished())
						{
							session.getFileScanOperation().joinThread();
						}
						if (!session.getFileScanOperation().aliveThread())
						{
							session.getFileScanOperation().setFinished(false);
							session.getFileScanOperation().setTarget(new File(splitCommand[2]));
							session.getFileScanOperation().setOperation(VTServerFileScanOperation.LIST_FILES);
							session.getFileScanOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file inspection is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 2)
				{
					
					if (splitCommand[1].toUpperCase().startsWith("S"))
					{
						if (session.getFileScanOperation().isFinished())
						{
							session.getFileScanOperation().joinThread();
						}
						if (session.getFileScanOperation().aliveThread())
						{
							connection.getResultWriter().write("\nVT>Trying to interrupt remote file inspection!\nVT>");
							connection.getResultWriter().flush();
							session.getFileScanOperation().interruptThread();
							session.getFileScanOperation().stopThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>No remote file inspection is running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						if (session.getFileScanOperation().isFinished())
						{
							session.getFileScanOperation().joinThread();
						}
						if (!session.getFileScanOperation().aliveThread())
						{
							session.getFileScanOperation().setFinished(false);
							session.getFileScanOperation().setTarget(new VTRootList());
							session.getFileScanOperation().setOperation(VTServerFileScanOperation.LIST_FILES);
							session.getFileScanOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file inspection is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 1)
				{
					if (session.getFileScanOperation().isFinished())
					{
						session.getFileScanOperation().joinThread();
					}
					if (!session.getFileScanOperation().aliveThread())
					{
						connection.getResultWriter().write("\nVT>No remote file inspection is running!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>A remote file inspection is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTFILEMODIFY") || splitCommand[0].equalsIgnoreCase("*VTFM"))
		{
			synchronized (session.getFileModifyOperation())
			{
				if (splitCommand.length >= 4)
				{
					if (splitCommand[1].toUpperCase().startsWith("M"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							session.getFileModifyOperation().setFinished(false);
							session.getFileModifyOperation().setSourceFile(new File(splitCommand[2]));
							session.getFileModifyOperation().setDestinationFile(new File(splitCommand[3]));
							session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.MOVE_FILE);
							session.getFileModifyOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("C"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							session.getFileModifyOperation().setFinished(false);
							session.getFileModifyOperation().setSourceFile(new File(splitCommand[2]));
							session.getFileModifyOperation().setDestinationFile(new File(splitCommand[3]));
							session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.COPY_FILE);
							session.getFileModifyOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 3)
				{
					if (splitCommand[1].toUpperCase().startsWith("F"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							session.getFileModifyOperation().setFinished(false);
							session.getFileModifyOperation().setSourceFile(new File(splitCommand[2]));
							session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.CREATE_FILE);
							session.getFileModifyOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("D"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							session.getFileModifyOperation().setFinished(false);
							session.getFileModifyOperation().setSourceFile(new File(splitCommand[2]));
							session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.CREATE_DIRECTORY);
							session.getFileModifyOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							session.getFileModifyOperation().setFinished(false);
							session.getFileModifyOperation().setSourceFile(new File(splitCommand[2]));
							session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.REMOVE_FILE);
							session.getFileModifyOperation().startThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("S"))
					{
						if (session.getFileModifyOperation().isFinished())
						{
							session.getFileModifyOperation().joinThread();
						}
						if (!session.getFileModifyOperation().aliveThread())
						{
							connection.getResultWriter().write("\nVT>Trying to interrupt remote file modification!\nVT>");
							connection.getResultWriter().flush();
							session.getFileModifyOperation().interruptThread();
							session.getFileModifyOperation().stopThread();
						}
						else
						{
							connection.getResultWriter().write("\nVT>No remote file modification is running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand.length == 1)
				{
					if (session.getFileModifyOperation().isFinished())
					{
						session.getFileModifyOperation().joinThread();
					}
					if (!session.getFileModifyOperation().aliveThread())
					{
						connection.getResultWriter().write("\nVT>No remote file modification is running!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>A remote file modification is still running!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
//		else if (splitCommand[0].equalsIgnoreCase("*VTCLOSESHELL") || splitCommand[0].equalsIgnoreCase("*VTCLSH"))
//		{
//			// connection.getResultWriter().write(command);
//			// connection.getResultWriter().flush();
//			synchronized (session.getShellExitListener())
//			{
//				if (!session.getShellExitListener().isStopped() && session.getShellExitListener().aliveThread())
//				{
//					connection.getResultWriter().write("\nVT>Stopping external shell...\nVT>");
//					connection.getResultWriter().flush();
//					session.setStoppingShell(true);
//					session.stopShell();
//					session.tryStopShellThreads();
//				}
//				else
//				{
//					connection.getResultWriter().write("\nVT>External shell is still stopped!\nVT>");
//					connection.getResultWriter().flush();
//				}
//			}
//			session.waitShell();
//			session.waitShellThreads();
//		}
		else if (splitCommand[0].equalsIgnoreCase("*VTBELL") || splitCommand[0].equalsIgnoreCase("*VTBL"))
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			connection.getResultWriter().write("\nVT>Invoking server terminal bell...\nVT>");
			connection.getResultWriter().flush();
			VTConsole.bell();
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTZIP") || splitCommand[0].equalsIgnoreCase("*VTZP"))
		{
			if (splitCommand.length >= 2 && splitCommand[1].toUpperCase().startsWith("R"))
			{
				synchronized (session.getZipFileOperation())
				{
					// connection.getResultWriter().write(command);
					// connection.getResultWriter().flush();
					if (splitCommand.length == 3)
					{
						if (splitCommand[2].toUpperCase().startsWith("S"))
						{
							if (session.getZipFileOperation().isFinished())
							{
								session.getZipFileOperation().joinThread();
							}
							if (session.getZipFileOperation().aliveThread())
							{
								connection.getResultWriter().write("\nVT>Trying to interrupt remote zip file operation!\nVT>");
								connection.getResultWriter().flush();
								session.getZipFileOperation().interruptThread();
								session.getZipFileOperation().stopThread();
							}
							else
							{
								connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
								connection.getResultWriter().flush();
							}
						}
						else
						{
							connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length == 2)
					{
						if (session.getZipFileOperation().isFinished())
						{
							session.getZipFileOperation().joinThread();
						}
						if (session.getZipFileOperation().aliveThread())
						{
							connection.getResultWriter().write("\nVT>A remote zip file operation is still running!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 5)
					{
						if (session.getZipFileOperation().isFinished())
						{
							session.getZipFileOperation().joinThread();
						}
						if (!session.getZipFileOperation().aliveThread())
						{
							if (splitCommand[2].toUpperCase().startsWith("C"))
							{
								session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_COMPRESS);
							}
							else if (splitCommand[2].toUpperCase().startsWith("U"))
							{
								session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_UNCOMPRESS);
							}
							else
							{
								session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_DECOMPRESS);
							}
							session.getZipFileOperation().setFinished(false);
							session.getZipFileOperation().setZipFilePath(splitCommand[3]);
							session.getZipFileOperation().setSourcePaths(splitCommand[4].split(";"));
							session.getZipFileOperation().startThread();
						}
						else if (splitCommand[2].toUpperCase().startsWith("S"))
						{
							if (session.getZipFileOperation().aliveThread())
							{
								connection.getResultWriter().write("\nVT>Trying to interrupt remote zip file operation!\nVT>");
								connection.getResultWriter().flush();
								session.getZipFileOperation().interruptThread();
								session.getZipFileOperation().stopThread();
							}
							else
							{
								connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
								connection.getResultWriter().flush();
							}
						}
						else
						{
							connection.getResultWriter().write("\nVT>Another remote zip file operation is still running!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTCHAINS") || splitCommand[0].equalsIgnoreCase("*VTCNS"))
		{
			if (splitCommand.length == 1)
			{
				int level = 1;
				connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
				connection.getResultWriter().flush();
				session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
				session.getShellCommandExecutor().flush();
			}
			else if (splitCommand.length == 2)
			{
				int level = Integer.parseInt(splitCommand[1]);
				connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
				connection.getResultWriter().flush();
				session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
				session.getShellCommandExecutor().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTACCESS") || splitCommand[0].equalsIgnoreCase("*VTAC"))
		{
			if (splitCommand.length == 1)
			{
				message.setLength(0);
				int sessionsLimit = session.getServer().getServerConnector().getSessionsLimit();
				String hostAddress = session.getServer().getServerConnector().getAddress();
				Integer port = session.getServer().getServerConnector().getPort();
				String proxyType = session.getServer().getServerConnector().getProxyType();
				String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
				Integer proxyPort = session.getServer().getServerConnector().getProxyPort();
				String proxyUser = session.getServer().getServerConnector().getProxyUser();
				String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
				String encryptionType = session.getServer().getServerConnector().getEncryptionType();
				String encryptionPassword = "";
				Integer natPort = session.getServer().getServerConnector().getNatPort();
				if (session.getServer().getServerConnector().getEncryptionKey() != null)
				{
					encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
				}
				message.append("\nVT>List of connection settings on server:\nVT>");
				if (session.getServer().getServerConnector().isPassive())
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
					message.append("\nVT>Encryption type(ET): [RC4]");
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
				if (session.getServer().getServerConnector().isUseProxyAuthentication())
				{
					message.append("\nVT>Proxy authentication(PA): [Enabled]");
				}
				else
				{
					message.append("\nVT>Proxy authentication(PA): [Disabled]");
				}
				message.append("\nVT>Proxy user(PU): [" + proxyUser + "]");
				message.append("\nVT>Proxy password(PK): [" + proxyPassword + "]");
				message.append("\nVT>Sessions limit(SL): [" + sessionsLimit + "]");
				message.append("\nVT>\nVT>End of connection settings list on server\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
				message.setLength(0);
			}
			else if (splitCommand.length >= 2)
			{
				if (splitCommand[1].equalsIgnoreCase("SF"))
				{
					if (splitCommand.length == 2)
					{
						try
						{
							session.getServer().saveServerSettingsFile("variable-terminal-server.properties");
							connection.getResultWriter().write("\nVT>Saved settings file:[variable-terminal-server.properties]");
							connection.getResultWriter().flush();
						}
						catch (Throwable t)
						{
							connection.getResultWriter().write("\nVT>Failed to save settings file:[variable-terminal-server.properties]");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						try
						{
							session.getServer().saveServerSettingsFile(splitCommand[2]);
							connection.getResultWriter().write("\nVT>Saved settings file:[" + splitCommand[2] + "]");
							connection.getResultWriter().flush();
						}
						catch (Throwable t)
						{
							connection.getResultWriter().write("\nVT>Failed to save settings file:[" + splitCommand[2] + "]");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("LF"))
				{
					if (splitCommand.length == 2)
					{
						try
						{
							session.getServer().loadServerSettingsFile("variable-terminal-server.properties");
							VTServerConnector connector = session.getServer().getServerConnector();
							synchronized (connector)
							{
								connector.interruptConnector();
								connector.notify();
							}
							connection.getResultWriter().write("\nVT>Loaded settings file:[variable-terminal-server.properties]");
							connection.getResultWriter().flush();
						}
						catch (Throwable t)
						{
							connection.getResultWriter().write("\nVT>Failed to load settings file:[variable-terminal-server.properties]");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						try
						{
							session.getServer().loadServerSettingsFile(splitCommand[2]);
							VTServerConnector connector = session.getServer().getServerConnector();
							synchronized (connector)
							{
								connector.interruptConnector();
								connector.notify();
							}
							connection.getResultWriter().write("\nVT>Loaded settings file:[" + splitCommand[2] + "]");
							connection.getResultWriter().flush();
						}
						catch (Throwable t)
						{
							connection.getResultWriter().write("\nVT>Failed to load settings file:[" + splitCommand[2] + "]");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("SL"))
				{
					if (splitCommand.length == 2)
					{
						int sessionsLimit = session.getServer().getServerConnector().getSessionsLimit();
						connection.getResultWriter().write("\nVT>Sessions limit(SL): [" + sessionsLimit + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						int sessionsLimit = Integer.parseInt(splitCommand[2]);
						if (sessionsLimit < 0)
						{
							sessionsLimit = 0;
						}
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setSessionsLimit(sessionsLimit);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Sessions limit(SL) set to: [" + sessionsLimit + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("CM"))
				{
					if (splitCommand.length == 2)
					{
						if (session.getServer().getServerConnector().isPassive())
						{
							connection.getResultWriter().write("\nVT>Connection mode(CM): [Passive]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Connection mode(CM): [Active]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						boolean passive = !splitCommand[2].toUpperCase().startsWith("A");
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setPassive(passive);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Connection mode(CM) set to: [" + (passive ? "Passive" : "Active") + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("CH"))
				{
					if (splitCommand.length == 2)
					{
						String hostAddress = session.getServer().getServerConnector().getAddress();
						connection.getResultWriter().write("\nVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						String hostAddress = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setAddress(hostAddress);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Connection host address(CH) set to: [" + hostAddress + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("CP"))
				{
					if (splitCommand.length == 2)
					{
						Integer port = session.getServer().getServerConnector().getPort();
						connection.getResultWriter().write("\nVT>Connection host port(CP): [" + port + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						try
						{
							int port = Integer.parseInt(splitCommand[2]);
							if (port < 1 || port > 65535)
							{
								connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								connection.getResultWriter().flush();
							}
							else
							{
								VTServerConnector connector = session.getServer().getServerConnector();
								synchronized (connector)
								{
									connector.setPort(port);
									connector.interruptConnector();
									connector.notify();
								}
								connection.getResultWriter().write("\nVT>Connection host port(CP) set to: [" + port + "]\nVT>");
								connection.getResultWriter().flush();
							}
						}
						catch (NumberFormatException e)
						{
							connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PT"))
				{
					if (splitCommand.length == 2)
					{
						String proxyType = session.getServer().getServerConnector().getProxyType();
						if (proxyType == null)
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT): [None]\nVT>");
							connection.getResultWriter().flush();
						}
						else if (proxyType.toUpperCase().startsWith("H"))
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT): [HTTP]\nVT>");
							connection.getResultWriter().flush();
						}
						else if (proxyType.toUpperCase().startsWith("S"))
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT): [SOCKS]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT): [None]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						String proxyType = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setProxyType(proxyType);
							connector.interruptConnector();
							connector.notify();
						}
						if (proxyType.toUpperCase().startsWith("H"))
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [HTTP]\nVT>");
							connection.getResultWriter().flush();
						}
						else if (proxyType.toUpperCase().startsWith("S"))
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [SOCKS]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [None]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PH"))
				{
					if (splitCommand.length == 2)
					{
						String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
						connection.getResultWriter().write("\nVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						String proxyAddress = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setProxyAddress(proxyAddress);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Proxy host address set to: [" + proxyAddress + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PP"))
				{
					if (splitCommand.length == 2)
					{
						Integer proxyPort = session.getServer().getServerConnector().getProxyPort();
						if (proxyPort != null)
						{
							connection.getResultWriter().write("\nVT>Proxy host port(PP): [" + proxyPort + "]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Proxy host port(PP): []\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						try
						{
							int proxyPort = Integer.parseInt(splitCommand[2]);
							if (proxyPort < 1 || proxyPort > 65535)
							{
								connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								connection.getResultWriter().flush();
							}
							else
							{
								VTServerConnector connector = session.getServer().getServerConnector();
								synchronized (connector)
								{
									connector.setProxyPort(proxyPort);
									connector.interruptConnector();
									connector.notify();
								}
								connection.getResultWriter().write("\nVT>Proxy host port(PP) set to: [" + proxyPort + "]\nVT>");
								connection.getResultWriter().flush();
							}
						}
						catch (NumberFormatException e)
						{
							connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PA"))
				{
					if (splitCommand.length == 2)
					{
						if (!session.getServer().getServerConnector().isUseProxyAuthentication())
						{
							connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Disabled]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Enabled]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						if (splitCommand[2].toUpperCase().startsWith("E"))
						{
							VTServerConnector connector = session.getServer().getServerConnector();
							synchronized (connector)
							{
								connector.setUseProxyAuthentication(true);
								connector.interruptConnector();
								connector.notify();
							}
							connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							VTServerConnector connector = session.getServer().getServerConnector();
							synchronized (connector)
							{
								connector.setUseProxyAuthentication(false);
								connector.interruptConnector();
								connector.notify();
							}
							connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PU"))
				{
					if (splitCommand.length == 2)
					{
						String proxyUser = session.getServer().getServerConnector().getProxyUser();
						connection.getResultWriter().write("\nVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						String proxyUser = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setProxyUser(proxyUser);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Proxy user(PU) set to: [" + proxyUser + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("PK"))
				{
					if (splitCommand.length == 2)
					{
						String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
						connection.getResultWriter().write("\nVT>Proxy password(PK): [" + proxyPassword + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						String proxyPassword = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setProxyPassword(proxyPassword);
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Proxy password(PK) set to: [" + proxyPassword + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("ET"))
				{
					if (splitCommand.length == 2)
					{
						String encryptionType = session.getServer().getServerConnector().getEncryptionType();
						if (encryptionType.toUpperCase().startsWith("R"))
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET): [RC4]\nVT>");
							connection.getResultWriter().flush();
						}
						else if (encryptionType.toUpperCase().startsWith("A"))
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET): [AES]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET): [None]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						String encryptionType = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setEncryptionType(encryptionType);
							connector.interruptConnector();
							connector.notify();
						}
						if (encryptionType.toUpperCase().startsWith("R"))
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [RC4]\nVT>");
							connection.getResultWriter().flush();
						}
						else if (encryptionType.toUpperCase().startsWith("A"))
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [AES]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [None]\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("EK"))
				{
					if (splitCommand.length == 2)
					{
						String encryptionPassword = "";
						if (session.getServer().getServerConnector().getEncryptionKey() != null)
						{
							encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
						}
						connection.getResultWriter().write("\nVT>Encryption password(EK): [" + encryptionPassword + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else if (splitCommand.length >= 3)
					{
						String encryptionPassword = splitCommand[2];
						VTServerConnector connector = session.getServer().getServerConnector();
						synchronized (connector)
						{
							connector.setEncryptionKey(encryptionPassword.getBytes("UTF-8"));
							connector.interruptConnector();
							connector.notify();
						}
						connection.getResultWriter().write("\nVT>Encryption password(EK) set to: [" + encryptionPassword + "]\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else if (splitCommand[1].equalsIgnoreCase("NP"))
				{
					if (splitCommand.length == 2)
					{
						Integer natPort = session.getServer().getServerConnector().getNatPort();
						if (natPort != null)
						{
							connection.getResultWriter().write("\nVT>Connection nat port(NP): [" + natPort + "]\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Connection nat port(NP): []\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else if (splitCommand.length >= 3)
					{
						try
						{
							int natPort = Integer.parseInt(splitCommand[2]);
							if (natPort < 1 || natPort > 65535)
							{
								connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								connection.getResultWriter().flush();
							}
							else
							{
								VTServerConnector connector = session.getServer().getServerConnector();
								if (natPort == 0)
								{
									synchronized (connector)
									{
										connector.setNatPort(null);
									}
									connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: []\nVT>");
									connection.getResultWriter().flush();
								}
								else
								{
									synchronized (connector)
									{
										connector.setNatPort(natPort);
									}
									connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: [" + natPort + "]\nVT>");
									connection.getResultWriter().flush();
								}
								
							}
						}
						catch (NumberFormatException e)
						{
							VTServerConnector connector = session.getServer().getServerConnector();
							synchronized (connector)
							{
								connector.setNatPort(null);
							}
							connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: []\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
		}
//		else if (splitCommand[0].equalsIgnoreCase("*VTRUNTIMEDIRECTORY") || splitCommand[0].equalsIgnoreCase("*VTRD"))
//		{
//			if (splitCommand.length == 1)
//			{
//				if (session.getRuntimeBuilderWorkingDirectory() != null)
//				{
//					connection.getResultWriter().write("\nVT>Server runtime working directory: [" + session.getRuntimeBuilderWorkingDirectory() + "]\nVT>");
//					connection.getResultWriter().flush();
//				}
//				else
//				{
//					connection.getResultWriter().write("\nVT>Server runtime working directory: []\nVT>");
//					connection.getResultWriter().flush();
//				}
//			}
//			else if (splitCommand.length >= 2)
//			{
//				if (splitCommand[1].length() > 0)
//				{
//					File workingDirectory = new File(splitCommand[1]);
//					if (workingDirectory.isDirectory())
//					{
//						session.setRuntimeBuilderWorkingDirectory(workingDirectory);
//						connection.getResultWriter().write("\nVT>Server runtime working directory set to: [" + workingDirectory + "]\nVT>");
//						connection.getResultWriter().flush();
//					}
//					else
//					{
//						connection.getResultWriter().write("\nVT>File path: [" + workingDirectory + "] is not a valid directory on server!\nVT>");
//						connection.getResultWriter().flush();
//					}
//				}
//				else
//				{
//					session.setRuntimeBuilderWorkingDirectory(null);
//					connection.getResultWriter().write("\nVT>Server runtime working directory set to: []\nVT>");
//					connection.getResultWriter().flush();
//				}
//			}
//			else
//			{
//				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
//				connection.getResultWriter().flush();
//			}
//		}
		else if (splitCommand[0].equalsIgnoreCase("*VTTCPTUNNEL") || splitCommand[0].equalsIgnoreCase("*VTTCPTN"))
		{
			if (splitCommand.length == 1)
			{
				message.setLength(0);
				for (VTTunnelChannelSocketListener channel : session.getTCPTunnelsServerHandler().getConnection().getChannels())
				{
					message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]"
					+ "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]"
					+ "\nVT>");
				}
				message.append("\nVT>End of connection TCP tunnels list\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length == 2)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsServerHandler().getConnection().getChannels();
					message.setLength(0);
					message.append("\nVT>List of server connection TCP tunnels:\nVT>");
					for (VTTunnelChannelSocketListener channel : channels)
					{
						message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
					}
					message.append("\nVT>End of server connection TCP tunnels list\nVT>");
					connection.getResultWriter().write(message.toString());
					connection.getResultWriter().flush();
				}
			}
			else if (splitCommand.length == 3)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						int bindPort = Integer.parseInt(splitCommand[2]);
						VTTunnelChannelSocketListener channel = session.getTCPTunnelsServerHandler().getConnection().getChannelSocketListener("", bindPort);
						if (channel != null)
						{
							channel.close();
							session.getTCPTunnelsServerHandler().getConnection().removeChannel(channel);
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] not found!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						
					}
					catch (Throwable e)
					{
						
					}
				}
				else
				{
					
				}
			}
			else if (splitCommand.length == 4)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						int bindPort = Integer.parseInt(splitCommand[2]);
						int redirectPort = Integer.parseInt(splitCommand[3]);
						if (bindPort < 1 || bindPort > 65535)
						{
							
						}
						else
						{
							session.getTCPTunnelsServerHandler().getConnection().setTCPChannel("", bindPort, "", redirectPort);
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (Throwable e1)
					{
						try
						{
							String redirectAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							
							VTTunnelChannelSocketListener channel = session.getTCPTunnelsServerHandler().getConnection().getChannelSocketListener(redirectAddress, bindPort);
							if (channel != null)
							{
								channel.close();
								session.getTCPTunnelsServerHandler().getConnection().removeChannel(channel);
								connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								connection.getResultWriter().flush();
							}
							else
							{
								connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + redirectAddress + " " + bindPort + "] not found!\nVT>");
								connection.getResultWriter().flush();
							}
						}
						catch (NumberFormatException e)
						{
							
						}
						catch (Throwable e)
						{
							
						}
					}
				}
			}
			else if (splitCommand.length == 5)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						int bindPort = Integer.parseInt(splitCommand[2]);
						String redirectAddress = splitCommand[3];
						int redirectPort = Integer.parseInt(splitCommand[4]);
						
						if (session.getTCPTunnelsServerHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
						{
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						try
						{
							String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							int redirectPort = Integer.parseInt(splitCommand[4]);
							
							if (session.getTCPTunnelsServerHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
							{
								connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
								connection.getResultWriter().flush();
							}
							else
							{
								connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
								connection.getResultWriter().flush();
							}
						}
						catch (Throwable e1)
						{
							
						}
					}
					catch (Throwable e)
					{
						
					}
				}
				else
				{
					
				}
			}
			else if (splitCommand.length >= 6)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						String bindAddress = splitCommand[2];
						int bindPort = Integer.parseInt(splitCommand[3]);
						String redirectAddress = splitCommand[4];
						int redirectPort = Integer.parseInt(splitCommand[5]);
						
						if (session.getTCPTunnelsServerHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
						{
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (Throwable e)
					{
						
					}
				}
			}
			else
			{
				
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTSOCKSTUNNEL") || splitCommand[0].equalsIgnoreCase("*VTSCKTN"))
		{
			if (splitCommand.length == 1)
			{
				message.setLength(0);
				for (VTTunnelChannelSocketListener channel : session.getSOCKSTunnelsServerHandler().getConnection().getChannels())
				{
					message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
				}
				message.append("\nVT>End of connection SOCKS tunnels list\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
			}
			else if (splitCommand.length == 2)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					Set<VTTunnelChannelSocketListener> channels = session.getSOCKSTunnelsServerHandler().getConnection().getChannels();
					message.setLength(0);
					message.append("\nVT>List of server connection SOCKS tunnels:\nVT>");
					for (VTTunnelChannelSocketListener channel : channels)
					{
						message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
					}
					message.append("\nVT>End of server connection SOCKS tunnels list\nVT>");
					connection.getResultWriter().write(message.toString());
					connection.getResultWriter().flush();
				}
			}
			else if (splitCommand.length == 3)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						int bindPort = Integer.parseInt(splitCommand[2]);
						VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsServerHandler().getConnection().getChannelSocketListener("", bindPort);
						if (channel != null)
						{
							channel.close();
							session.getSOCKSTunnelsServerHandler().getConnection().removeChannel(channel);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							session.getSOCKSTunnelsServerHandler().getConnection().setSOCKSChannel("", bindPort);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						
					}
					catch (Throwable e)
					{
						
					}
				}
				else
				{
					
				}
			}
			else if (splitCommand.length == 4)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						String bindAddress = splitCommand[2];
						int bindPort = Integer.parseInt(splitCommand[3]);
						VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsServerHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
						if (channel != null)
						{
							channel.close();
							session.getSOCKSTunnelsServerHandler().getConnection().removeChannel(channel);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							session.getSOCKSTunnelsServerHandler().getConnection().setSOCKSChannel(bindAddress, bindPort);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						
					}
					catch (Throwable e)
					{
						
					}
				}
			}
			else if (splitCommand.length == 5)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						int bindPort = Integer.parseInt(splitCommand[2]);
						String socksUsername = splitCommand[3];
						String socksPassword = splitCommand[4];
						VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsServerHandler().getConnection().getChannelSocketListener("", bindPort);
						if (channel != null)
						{
							channel.close();
							session.getSOCKSTunnelsServerHandler().getConnection().removeChannel(channel);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							session.getSOCKSTunnelsServerHandler().getConnection().setSOCKSChannel("", bindPort, socksUsername, socksPassword);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						
					}
					catch (Throwable e)
					{
						
					}
				}
				else
				{
					
				}
			}
			else if (splitCommand.length >= 6)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					try
					{
						String bindAddress = splitCommand[2];
						int bindPort = Integer.parseInt(splitCommand[3]);
						String socksUsername = splitCommand[4];
						String socksPassword = splitCommand[5];
						
						VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsServerHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
						if (channel != null)
						{
							channel.close();
							session.getSOCKSTunnelsServerHandler().getConnection().removeChannel(channel);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							session.getSOCKSTunnelsServerHandler().getConnection().setSOCKSChannel(bindAddress, bindPort, socksUsername, socksPassword);
							connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (IndexOutOfBoundsException e)
					{
						
					}
					catch (NumberFormatException e)
					{
						
					}
					catch (Throwable e)
					{
						
					}
				}
				else
				{
					
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTSCREENALERT") || splitCommand[0].equalsIgnoreCase("*VTSA"))
		{
			if (!GraphicsEnvironment.isHeadless())
			{
				if (splitCommand.length == 2)
				{
					// splitCommand[1] =
					// StringEscapeUtils.unescapeJava(splitCommand[1]);
					if (splitCommand[1].indexOf('/') < 0)
					{
						VTGraphicsMessager.showAlert(VTGraphicalConsole.getFrame(), "Variable-Terminal Server", splitCommand[1]);
						session.getServer().displayTrayIconMessage("Variable-Terminal Server", "["+ splitCommand[1] + "]");
					}
					else
					{
						String message = splitCommand[1].substring(0, splitCommand[1].indexOf('/'));
						String title = splitCommand[1].substring(splitCommand[1].indexOf('/') + 1);
						VTGraphicsMessager.showAlert(VTGraphicalConsole.getFrame(), title, message);
						session.getServer().displayTrayIconMessage(title, "[" + message + "]");
					}
					connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
					connection.getResultWriter().flush();
				}
				else if (splitCommand.length >= 3)
				{
					try
					{
						// splitCommand[1] =
						// StringEscapeUtils.unescapeJava(splitCommand[1]);
						// splitCommand[2] =
						// StringEscapeUtils.unescapeJava(splitCommand[2]);
						final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
						int number = Integer.parseInt(splitCommand[2]);
						if (number < devices.length)
						{
							if (number <= -1)
							{
								number = 0;
							}
							if (splitCommand[1].indexOf('/') < 0)
							{
								VTGraphicsMessager.showAlert(devices[number], VTGraphicalConsole.getFrame(), "Variable-Terminal Server", splitCommand[1]);
								session.getServer().displayTrayIconMessage("Variable-Terminal Server", "[" + splitCommand[1] + "]");
							}
							else
							{
								String message = splitCommand[1].substring(0, splitCommand[1].indexOf('/'));
								String title = splitCommand[1].substring(splitCommand[1].indexOf('/') + 1);
								VTGraphicsMessager.showAlert(devices[number], VTGraphicalConsole.getFrame(), title, message);
								session.getServer().displayTrayIconMessage(title, "[" + message + "]");
							}
						}
						else
						{
							connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					catch (NumberFormatException e)
					{
						connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Graphical alert not supported in server!\nVT>");
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTBROWSE") || splitCommand[0].equalsIgnoreCase("*VTBRWS"))
		{
			if (splitCommand.length >= 2)
			{
				try
				{
					Class.forName("java.awt.Desktop");
					Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
					if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
					{
						desktop.browse(new URI(splitCommand[1]));
						connection.getResultWriter().write("\nVT>Browse operation executed!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Browse operation not supported!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (SecurityException e)
				{
					connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (IllegalArgumentException e)
				{
					connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (IOException e)
				{
					connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (Throwable e)
				{
					connection.getResultWriter().write("\nVT>Browse operation not supported!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTRUNPRINT") || splitCommand[0].equalsIgnoreCase("*VTRPR"))
		{
			if (splitCommand.length >= 2)
			{
				try
				{
					Class.forName("java.awt.Desktop");
					Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
					if (desktop != null && desktop.isSupported(Desktop.Action.PRINT))
					{
						desktop.print(new File(splitCommand[1]));
						connection.getResultWriter().write("\nVT>Print operation executed!\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Print operation not supported!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (SecurityException e)
				{
					connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (IllegalArgumentException e)
				{
					connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (IOException e)
				{
					connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
					connection.getResultWriter().flush();
				}
				catch (Throwable e)
				{
					connection.getResultWriter().write("\nVT>Print operation not supported!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTAUDIOLINK") || splitCommand[0].equalsIgnoreCase("*VTAL"))
		{
			if (!session.isRunningAudio())
			{
				Mixer.Info inputMixer = null;
				Mixer.Info outputMixer = null;
				if (splitCommand.length >= 2)
				{
					Mixer.Info[] info = AudioSystem.getMixerInfo();
					for (int i = 1; i < splitCommand.length; i += 1)
					{
						String[] parameters = splitCommand[i].split("/");
						if (parameters.length >= 3)
						{
							String side = parameters[0];
							String type = parameters[1];
							String mixer = parameters[2];
							if (side.toUpperCase().startsWith("L"))
							{
								if (type.toUpperCase().startsWith("I"))
								{
									
								}
								else if (type.toUpperCase().startsWith("O"))
								{
									
								}
								else
								{
									
								}
							}
							else if (side.toUpperCase().startsWith("R"))
							{
								if (type.toUpperCase().startsWith("I"))
								{
									try
									{
										int index = Integer.parseInt(mixer);
										inputMixer = info[index];
									}
									catch (Throwable t)
									{
										connection.getResultWriter().write("\nVT>Invalid remote input audio mixer number [" + mixer + "]!");
										connection.getResultWriter().flush();
										// ok = false;
									}
								}
								else if (type.toUpperCase().startsWith("O"))
								{
									try
									{
										int index = Integer.parseInt(mixer);
										outputMixer = info[index];
									}
									catch (Throwable t)
									{
										connection.getResultWriter().write("\nVT>Invalid remote output audio mixer number [" + mixer + "]!");
										connection.getResultWriter().flush();
										// ok = false;
									}
								}
								else
								{
									
								}
							}
							else
							{
								
							}
						}
						else
						{
							
						}
					}
				}
				
				connection.resetAudioStreams();
				if (!session.getServer().getAudioSystem().isRunning())
				{
					session.getServer().getAudioSystem().initialize(VT.VT_AUDIO_FORMAT);
				}
				if (session.getServer().getAudioSystem().addAudioPlay(connection.getAudioDataInputStream(), outputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
				{
					connection.getAudioControlOutputStream().write(1);
					connection.getAudioControlOutputStream().flush();
					if (connection.getAudioControlInputStream().read() == 1)
					{
						if (session.getServer().getAudioSystem().addAudioCapture(connection.getAudioDataOutputStream(), inputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
						{
							connection.getAudioControlOutputStream().write(1);
							connection.getAudioControlOutputStream().flush();
							if (connection.getAudioControlInputStream().read() == 1)
							{
								session.setRunningAudio(true);
								connection.getResultWriter().write("\nVT>Remote audio link started!\nVT>");
								connection.getResultWriter().flush();
							}
						}
						else
						{
							connection.getAudioControlOutputStream().write(0);
							connection.getAudioControlOutputStream().flush();
							connection.closeAudioStreams();
							session.setRunningAudio(false);
							connection.getResultWriter().write("\nVT>Remote audio link start on server failed!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.closeAudioStreams();
						session.setRunningAudio(false);
					}
				}
				else
				{
					connection.getAudioControlOutputStream().write(0);
					connection.getAudioControlOutputStream().flush();
					connection.closeAudioStreams();
					session.setRunningAudio(false);
					connection.getResultWriter().write("\nVT>Remote audio link start on server failed!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else
			{
				connection.closeAudioStreams();
				session.setRunningAudio(false);
				connection.getResultWriter().write("\nVT>Remote audio link stopped!\nVT>");
				connection.getResultWriter().flush();
			}
		}
		
		else if (splitCommand[0].equalsIgnoreCase("*VTLOCK") || splitCommand[0].equalsIgnoreCase("*VTLK"))
		{
			if (splitCommand.length >= 3)
			{
				// int separator = splitCommand[1].indexOf('/');
				// if (separator >= 0)
				// {
				// String login = splitCommand[1].substring(0, separator);
				// String password = splitCommand[1].substring(separator + 1);
				// session.getServer().setUniqueUserCredential(login, password);
				// connection.getResultWriter().write("\nVT>Single credential
				// set!\nVT>");
				// connection.getResultWriter().flush();
				// }
				// else
				// {
				// connection.getResultWriter().write("\nVT>Invalid command
				// syntax!" +
				// VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				// connection.getResultWriter().flush();
				// }
				String login = splitCommand[1];
				String password = splitCommand[2];
				session.getServer().setUniqueUserCredential(login, password);
				connection.getResultWriter().write("\nVT>Single credential set!\nVT>");
				connection.getResultWriter().flush();
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTSHELL") || splitCommand[0].equalsIgnoreCase("*VTSH"))
		{
			if (splitCommand.length >= 2)
			{
				if (splitCommand[1].toUpperCase().contains("O"))
				{
					connection.getResultWriter().write("\nVT>Opening external shell...\nVT>");
					connection.getResultWriter().flush();
					session.setRestartingShell(true);
					session.restartShell();
				}
				else if (splitCommand[1].toUpperCase().contains("C"))
				{
					synchronized (session.getShellExitListener())
					{
						if (!session.getShellExitListener().isStopped() && session.getShellExitListener().aliveThread())
						{
							connection.getResultWriter().write("\nVT>Closing external shell...\nVT>");
							connection.getResultWriter().flush();
							session.setStoppingShell(true);
							session.stopShell();
							session.tryStopShellThreads();
						}
						else
						{
							connection.getResultWriter().write("\nVT>External shell already closed!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					session.waitShell();
					session.waitShellThreads();
				}
				else if (splitCommand[1].toUpperCase().contains("D"))
				{
					if (splitCommand.length >= 3 && (splitCommand[2].length() > 0))
					{
						String[] shell = new String[splitCommand.length - 2];
						for (int i = 0; i < shell.length; i++)
						{
							shell[i] = splitCommand[i + 1];
						}
						connection.getResultWriter().write("\nVT>Defining external shell to: [" + Arrays.toString(shell) + "]");
						connection.getResultWriter().flush();
						session.setShellBuilder(shell, null, null);
						session.restartShell();
					}
					else
					{
						session.setShellBuilder(null, null, null);
						connection.getResultWriter().write("\nVT>Defining external shell to: [Default]");
						connection.getResultWriter().flush();
						
						session.restartShell();
					}
				}
				else
				{
					connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					connection.getResultWriter().flush();
				}
				
				
			}
			else
			{
				connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTPING") || splitCommand[0].equalsIgnoreCase("*VTPNG"))
		{
			// session.getNanoPingService().interrupt();
			// VTTerminal.printf("\nVT>Current client/server network
			// connection latency:
			// %.2f ms\nVT>", estimated);
			// VTConsole.printf("\nVT>Current client/server connection
			// latency: [%d]
			// ms\nVT>", estimated);
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTAUDIOMIXERS") || splitCommand[0].equalsIgnoreCase("*VTAM"))
		{
			if (splitCommand.length >= 2)
			{
				if (splitCommand[1].toUpperCase().startsWith("R"))
				{
					message.setLength(0);
					message.append("\nVT>List of remote audio mixers:\nVT>");
					Mixer.Info[] mixers = AudioSystem.getMixerInfo();
					int number = 0;
					for (Mixer.Info info : mixers)
					{
						message.append("\nVT>Number: [" + number++ + "]");
						message.append("\nVT>Name: [" + info.getName() + "]");
						message.append("\nVT>Description: [" + info.getDescription() + "]");
						message.append("\nVT>");
					}
					message.append("\nVT>End of remote audio mixers list\nVT>");
					connection.getResultWriter().write(message.toString());
					connection.getResultWriter().flush();
				}
				else if (splitCommand[1].toUpperCase().startsWith("L"))
				{
					
				}
			}
			else
			{
				message.setLength(0);
				message.append("\nVT>List of remote audio mixers:\nVT>");
				Mixer.Info[] mixers = AudioSystem.getMixerInfo();
				int number = 0;
				for (Mixer.Info info : mixers)
				{
					message.append("\nVT>Number: [" + number++ + "]");
					message.append("\nVT>Name: [" + info.getName() + "]");
					message.append("\nVT>Description: [" + info.getDescription() + "]");
					message.append("\nVT>");
				}
				message.append("\nVT>End of remote audio mixers list\nVT>");
				connection.getResultWriter().write(message.toString());
				connection.getResultWriter().flush();
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTRATELIMIT") || splitCommand[0].equalsIgnoreCase("*VTRL"))
		{
			if (splitCommand.length == 1)
			{
				long rate = connection.getRateInBytesPerSecond();
				if (rate > 0)
				{
					connection.getResultWriter().write("\nVT>Connection download rate limit: [" + rate + "] bytes per second\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Connection download rate limit: [Unlimited] bytes per second\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else if (splitCommand.length == 2)
			{
				long rate = 0;
				try
				{
					rate = Long.parseLong(splitCommand[1]);
					if (rate <= 0)
					{
						rate = 0;
					}
					else if (rate < ((1024 + 8) * 8))
					{
						rate = ((1024 + 8) * 8);
					}
					connection.setRateInBytesPerSecond(rate);
					if (rate > 0)
					{
						connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
						connection.getResultWriter().flush();
					}
					else
					{
						connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (NumberFormatException e)
				{
					
				}
			}
			else if (splitCommand.length >= 3)
			{
				long rate = 0;
				try
				{
					rate = Long.parseLong(splitCommand[1]);
					if (rate <= 0)
					{
						rate = 0;
					}
					else if (rate < ((1024 + 8) * 8))
					{
						rate = ((1024 + 8) * 8);
					}
					if (!splitCommand[2].toUpperCase().startsWith("U"))
					{
						connection.setRateInBytesPerSecond(rate);
						if (rate > 0)
						{
							connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
							connection.getResultWriter().flush();
						}
						else
						{
							connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
							connection.getResultWriter().flush();
						}
					}
				}
				catch (NumberFormatException e)
				{
					
				}
			}
		}
		else if (splitCommand[0].equalsIgnoreCase("*VTCOVER") || splitCommand[0].equalsIgnoreCase("*VTCV"))
		{
			if (session.getServer().isDaemon())
			{
				connection.getResultWriter().write("\nVT>Server console interface is unavailable\nVT>");
				connection.getResultWriter().flush();
			}
			else
			{
				if (VTConsole.isDaemon())
				{
					session.getServer().enableTrayIcon();
					VTConsole.setDaemon(false);
					connection.getResultWriter().write("\nVT>Server console interface enabled\nVT>");
					connection.getResultWriter().flush();
				}
				else
				{
					session.getServer().disableTrayIcon();
					VTConsole.setDaemon(true);
					connection.getResultWriter().write("\nVT>Server console interface disabled\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
		/* else if (splitCommand[0].equalsIgnoreCase("*VTSCRIPT") ||
		 * splitCommand[0].equalsIgnoreCase("*VTSC")) { if (splitCommand.length
		 * >= 2) { String parameters =
		 * command.substring(splitCommand[0].length() + 1); String[] files =
		 * parameters.split(";"); for (String file : files) { if (file == null
		 * || file.length() < 1) { continue; } File scriptFile = new File(file);
		 * if (scriptFile.exists()) { executeFileScript(scriptFile, stack); } }
		 * } else {
		 * connection.getResultWriter().write("\nVT>Invalid command syntax!" +
		 * VTHelpManager.getHelpForClientCommand(splitCommand[0]));
		 * connection.getResultWriter().flush(); } } */
		/* else if (command.toUpperCase().startsWith("*VTSYSTEM") ||
		 * command.toUpperCase().startsWith("*VTSYS")) { if
		 * (splitCommand.length == 1) { Thread newThread = new Thread(new
		 * Runnable() { public void run() { try { int ret =
		 * VTNativeUtils.system(null);
		 * if (ret != 0) { connection.getResultWriter().
		 * write("\nVT>System command processor available on server!\nVT>");
		 * connection.getResultWriter().flush(); } else {
		 * connection.getResultWriter().
		 * write("\nVT>System command processor not available on server!\nVT>"
		 * ); connection.getResultWriter().flush(); } } catch (Throwable e) {
		 * } } }, "VTServerNativeSystemCall"); newThread.start(); } else if
		 * (splitCommand.length >= 2) { Thread newThread = new Thread(new
		 * Runnable() { public void run() {
		 * VTNativeUtils.system(command.substring(splitCommand[0].length() +
		 * 1)); } }, "VTServerNativeSystemCall"); newThread.start();
		 * connection.getResultWriter().write("\nVT>System command [" +
		 * splitCommand[1] + "] sent to server!\nVT>");
		 * connection.getResultWriter().flush(); } } */
		else if (!stopped)
		{
			if (command.startsWith("**") && command.toUpperCase().contains("*VT"))
			{
				try
				{
					session.getShellCommandExecutor().write(command.substring(1) + "\n");
					session.getShellCommandExecutor().flush();
				}
				catch (Throwable e)
				{
					
				}
			}
			else
			{
				try
				{
					/* if (!Platform.isWindows()) {
					 * connection.getResultWriter().write(command + "\n");
					 * connection.getResultWriter().flush(); } */
					session.getShellCommandExecutor().write(command + "\n");
					session.getShellCommandExecutor().flush();
				}
				catch (Throwable e)
				{
					
				}
			}
		}
	}
}