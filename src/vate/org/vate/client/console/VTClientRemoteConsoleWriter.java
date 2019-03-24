package org.vate.client.console;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.VT;
import org.vate.client.connection.VTClientConnection;
import org.vate.client.session.VTClientSession;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;
import org.vate.task.VTTask;
import org.vate.tunnel.channel.VTTunnelChannelSocketListener;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTClientRemoteConsoleWriter extends VTTask
{
	private StringBuilder message;
	private VTClientSession session;
	private VTClientConnection connection;
	
	public VTClientRemoteConsoleWriter(VTClientSession session)
	{
		this.session = session;
		this.connection = session.getConnection();
		this.message = new StringBuilder();
	}
	
	public void run()
	{
		// int p = 0;
		if (!stopped)
		{
			String commands = session.getClient().getClientConnector().getSessionCommands().trim();
			commands = commands.replace("*;", "\n");
			BufferedReader sessionCommandReader = new BufferedReader(new StringReader(commands));
			String line = null;
			// String command = null;
			try
			{
				while (!stopped && (line = sessionCommandReader.readLine()) != null)
				{
					try
					{
						executeCommand(line, null);
					}
					catch (Throwable t)
					{
						stopped = true;
						break;
					}
				}
			}
			catch (Throwable t)
			{
				// stopped = true;
			}
		}
		while (!stopped)
		{
			String[] lines;
			try
			{
				String line = VTConsole.readLine(true);
				if (line.contains("*;"))
				{
					lines = line.split("\\*;");
					for (String subLine : lines)
					{
						executeCommand(subLine, null);
					}
				}
				else
				{
					executeCommand(line, null);
				}
			}
			catch (InterruptedException e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
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
		if (session.getClient().getClientConnector().isSkipConfiguration())
		{
			stopped = true;
			return;
		}
		else if (command != null)
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
			if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
			{
				synchronized (session.getFileTransferClient().getHandler().getSession().getTransaction())
				{
					if (splitCommand.length == 2)
					{
						if (splitCommand[1].toUpperCase().startsWith("S"))
						{
							if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
							{
								session.getFileTransferClient().joinThread();
							}
							if (session.getFileTransferClient().aliveThread())
							{
								// VTTerminal.print(command);
								session.getFileTransferClient().getHandler().getSession().getTransaction().setInterrupted();
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
								session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(true);
							}
							else
							{
								// VTTerminal.print(command + "\nVT>No file
								// transfer is running!\nVT>");
								VTConsole.print("\nVT>No file transfer is running!\nVT>");
							}
						}
						/* else if
						 * (splitCommand[1].toUpperCase().startsWith("A")) { if
						 * (session.getFileTransferClient().getHandler().
						 * getSession().getTransfer().isFinished()) {
						 * session.getFileTransferClient().joinThread(); } if
						 * (session.getFileTransferClient().aliveThread()) {
						 * VTTerminal.
						 * print("\nVT>A file transfer is running!\nVT>"); }
						 * else { VTTerminal.
						 * print("\nVT>No file transfer is running!\nVT>"); }
						 * } */
						else
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand.length >= 4)
					{
						if (splitCommand[1].toUpperCase().contains("G") && !splitCommand[1].toUpperCase().contains("P"))
						{
							if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
							{
								session.getFileTransferClient().joinThread();
							}
							if (!session.getFileTransferClient().aliveThread())
							{
								session.getFileTransferClient().getHandler().getSession().getTransaction().setFinished(false);
								session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(false);
								session.getFileTransferClient().getHandler().getSession().getTransaction().setCommand(command);
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
								session.getFileTransferClient().startThread();
							}
							else
							{
								VTConsole.print("\nVT>Another file transfer is still running!\nVT>");
							}
						}
						else if (splitCommand[1].toUpperCase().contains("P") && !splitCommand[1].toUpperCase().contains("G"))
						{
							if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
							{
								session.getFileTransferClient().joinThread();
							}
							if (!session.getFileTransferClient().aliveThread())
							{
								session.getFileTransferClient().getHandler().getSession().getTransaction().setFinished(false);
								session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(false);
								session.getFileTransferClient().getHandler().getSession().getTransaction().setCommand(command);
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
								session.getFileTransferClient().startThread();
							}
							else
							{
								VTConsole.print("\nVT>Another file transfer is still running!\nVT>");
							}
						}
						else
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand.length == 1)
					{
						if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
						{
							session.getFileTransferClient().joinThread();
						}
						if (session.getFileTransferClient().aliveThread())
						{
							long transferDataSize = session.getFileTransferClient().getHandler().getSession().getTransaction().getTransferDataSize();
							long transferDataCount = session.getFileTransferClient().getHandler().getSession().getTransaction().getTransferDataCount();
							if (transferDataSize != 0 && transferDataCount != 0)
							{
								// double completeness =
								// ((double)transferDataCount) * 100 /
								// ((double)transferDataSize);
								// VTConsole.printf("\nVT>A file transfer is
								// running!" +
								// "\nVT>Transferred [" + transferDataCount +
								// "] of [" + transferDataSize + "] bytes
								// (%.2f%%)\nVT>", completeness);
								VTConsole.printf("\nVT>A file transfer is running!" + "\nVT>Transferred [" + transferDataCount + "] bytes!\nVT>");
							}
							else
							{
								VTConsole.print("\nVT>A file transfer is running!\nVT>");
							}
						}
						else
						{
							VTConsole.print("\nVT>No file transfer is running!\nVT>");
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTGRAPHICSLINK") || splitCommand[0].equalsIgnoreCase("*VTGL"))
			{
				if (GraphicsEnvironment.isHeadless())
				{
					VTConsole.print("\nVT>Remote graphics link start on client failed!\nVT>");
					return;
				}
				if (splitCommand.length >= 2)
				{
					/* if (splitCommand[1].toUpperCase().startsWith("S")) {
					 * synchronized (session.getGraphicsClient()) { if
					 * (session.getGraphicsClient().isFinished()) {
					 * session.getGraphicsClient().joinThread(); } } if
					 * (session.getGraphicsClient().aliveThread()) {
					 * } else { VTTerminal.
					 * print("\nVT>Remote graphics link is not running!\nVT>"
					 * ); } } */
					if (splitCommand[1].toUpperCase().startsWith("V"))
					{
						synchronized (session.getGraphicsClient())
						{
							if (session.getGraphicsClient().isFinished())
							{
								// System.out.println("session.getGraphicsClient().isFinished()");
								session.getGraphicsClient().joinThread();
								// System.out.println("session.getGraphicsClient().joinThread()");
							}
						}
						if (!session.getGraphicsClient().aliveThread())
						{
							session.getGraphicsClient().setFinished(false);
							session.getGraphicsClient().setReadOnly(true);
							connection.getCommandWriter().write(command + "\n");
							connection.getCommandWriter().flush();
							session.getGraphicsClient().startThread();
							// System.out.println("session.getGraphicsClient().startThread()");
						}
						else
						{
							session.getGraphicsClient().setReadOnly(true);
							VTConsole.print("\nVT>Remote graphics link set to view mode!\nVT>");
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("C"))
					{
						synchronized (session.getGraphicsClient())
						{
							if (session.getGraphicsClient().isFinished())
							{
								// System.out.println("session.getGraphicsClient().isFinished()");
								session.getGraphicsClient().joinThread();
								// System.out.println("session.getGraphicsClient().joinThread()");
							}
						}
						if (!session.getGraphicsClient().aliveThread())
						{
							session.getGraphicsClient().setFinished(false);
							session.getGraphicsClient().setReadOnly(false);
							connection.getCommandWriter().write(command + "\n");
							connection.getCommandWriter().flush();
							session.getGraphicsClient().startThread();
							// System.out.println("session.getGraphicsClient().startThread()");
						}
						else
						{
							session.getGraphicsClient().setReadOnly(false);
							VTConsole.print("\nVT>Remote graphics link set to control mode!\nVT>");
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length == 1)
				{
					synchronized (session.getGraphicsClient())
					{
						if (session.getGraphicsClient().isFinished())
						{
							// System.out.println("session.getGraphicsClient().isFinished()");
							session.getGraphicsClient().joinThread();
							// System.out.println("session.getGraphicsClient().joinThread()");
						}
					}
					if (session.getGraphicsClient().aliveThread())
					{
						// VTTerminal.print("\nVT>Remote graphics link is
						// running!\nVT>");
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
						session.getGraphicsClient().setStopped(true);
						session.getGraphicsClient().joinThread();
						// System.out.println("session.getGraphicsClient().joinThread()");
					}
					else
					{
						session.getGraphicsClient().setFinished(false);
						session.getGraphicsClient().setReadOnly(false);
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
						session.getGraphicsClient().startThread();
						// System.out.println("session.getGraphicsClient().startThread()");
					}
				}
				else
				{
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTDISCONNECT") || splitCommand[0].equalsIgnoreCase("*VTDSCT"))
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
				VTConsole.print("\nVT>Disconnecting from server...");
				// connection.setSkipLine(true);
				connection.closeSockets();
				stopped = true;
				return;
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTTERMINATE") || splitCommand[0].equalsIgnoreCase("*VTTMNT"))
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
				VTConsole.print("\nVT>Finalizing server...");
				// connection.setSkipLine(true);
				// connection.closeSockets();
				// stopped = true;
				// break;
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTQUIT") || splitCommand[0].equalsIgnoreCase("*VTQT"))
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
				VTConsole.print("\nVT>Finalizing client...");
				// connection.setSkipLine(true);
				connection.closeSockets();
				System.exit(0);
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTCLEAR") || splitCommand[0].equalsIgnoreCase("*VTCLR"))
			{
				VTConsole.clear();
				VTConsole.print("VT>");
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTZIP") || splitCommand[0].equalsIgnoreCase("*VTZP"))
			{
				if (splitCommand.length >= 2 && splitCommand[1].toUpperCase().startsWith("L"))
				{
					synchronized (session.getZipFileOperation())
					{
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
									VTConsole.print("\nVT>Trying to interrupt local zip file operation!\nVT>");
									session.getZipFileOperation().interruptThread();
									session.getZipFileOperation().stopThread();
								}
								else
								{
									VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
								}
							}
							else
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
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
								VTConsole.print("\nVT>A local zip file operation is still running!\nVT>");
							}
							else
							{
								VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
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
									VTConsole.print("\nVT>Trying to interrupt local zip file operation!\nVT>");
									session.getZipFileOperation().interruptThread();
									session.getZipFileOperation().stopThread();
								}
								else
								{
									VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
								}
							}
							else
							{
								VTConsole.print("\nVT>Another local zip file operation is still running!\nVT>");
							}
						}
						else
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
				}
				else if (splitCommand.length >= 3 && splitCommand[1].toUpperCase().startsWith("R"))
				{
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
				else
				{
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTTCPTUNNEL") || splitCommand[0].equalsIgnoreCase("*VTTCPTN"))
			{
				if (splitCommand.length == 1)
				{
					Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsHandler().getConnection().getChannels();
					message.setLength(0);
					message.append("\nVT>List of connection TCP tunnels:\nVT>");
					for (VTTunnelChannelSocketListener channel : channels)
					{
						message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]"
						+ "\nVT>Server TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]"
						+ "\nVT>");
					}
					VTConsole.print(message.toString());
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
				else if (splitCommand.length == 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsHandler().getConnection().getChannels();
						message.setLength(0);
						message.append("\nVT>List of client connection TCP tunnels:\nVT>");
						for (VTTunnelChannelSocketListener channel : channels)
						{
							message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
						}
						message.append("\nVT>End of client connection TCP tunnels list\nVT>");
						VTConsole.print(message.toString());
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
					}
				}
				else if (splitCommand.length == 3)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
								if (channel != null)
								{
									channel.close();
									session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>TCP tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] not found!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length == 4)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
								if (channel != null)
								{
									channel.close();
									session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>TCP tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] not found!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (Throwable e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							//String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length == 5)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								String redirectAddress = splitCommand[3];
								int redirectPort = Integer.parseInt(splitCommand[4]);
								if (redirectPort < 1 || redirectPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									if (session.getTCPTunnelsHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
									{
										VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
									}
									else
									{
										VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
									}
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							//VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							try
							{
								String bindAddress = splitCommand[2];
								int bindPort = Integer.parseInt(splitCommand[3]);
								if (bindPort < 1 || bindPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									int redirectPort = Integer.parseInt(splitCommand[4]);
									if (redirectPort < 1 || redirectPort > 65535)
									{
										VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
									}
									else
									{
										if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
										{
											VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
										}
										else
										{
											VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
										}
									}
								}
							}
							catch (Throwable e1)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
						}
						catch (Throwable e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								int redirectPort = Integer.parseInt(splitCommand[4]);
								if (redirectPort < 1 || redirectPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									connection.getCommandWriter().write(command + "\n");
									connection.getCommandWriter().flush();
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							//VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							try
							{
								int bindPort = Integer.parseInt(splitCommand[3]);
								if (bindPort < 1 || bindPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									int redirectPort = Integer.parseInt(splitCommand[4]);
									if (redirectPort < 1 || redirectPort > 65535)
									{
										VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
									}
									else
									{
										connection.getCommandWriter().write(command + "\n");
										connection.getCommandWriter().flush();
									}
								}
							}
							catch (Throwable e1)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length >= 6)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							String redirectAddress = splitCommand[4];
							int redirectPort = Integer.parseInt(splitCommand[5]);
							
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								if (redirectPort < 1 || redirectPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
									{
										VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
									}
									else
									{
										VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
									}
								}
							}
						}
						catch (Throwable e)
						{
							//e.printStackTrace();
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							//String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							//String redirectAddress = splitCommand[4];
							int redirectPort = Integer.parseInt(splitCommand[5]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								if (redirectPort < 1 || redirectPort > 65535)
								{
									VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								}
								else
								{
									connection.getCommandWriter().write(command + "\n");
									connection.getCommandWriter().flush();
								}
							}
						}
						catch (Throwable e)
						{
							//e.printStackTrace();
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else
				{
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTSOCKSTUNNEL") || splitCommand[0].equalsIgnoreCase("*VTSCKTN"))
			{
				if (splitCommand.length == 1)
				{
					Set<VTTunnelChannelSocketListener> channels = session.getSOCKSTunnelsHandler().getConnection().getChannels();
					message.setLength(0);
					message.append("\nVT>List of connection SOCKS tunnels:\nVT>");
					for (VTTunnelChannelSocketListener channel : channels)
					{
						message.append("\nVT>Client SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
					}
					VTConsole.print(message.toString());
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
				else if (splitCommand.length == 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						Set<VTTunnelChannelSocketListener> channels = session.getSOCKSTunnelsHandler().getConnection().getChannels();
						message.setLength(0);
						message.append("\nVT>List of client connection SOCKS tunnels:\nVT>");
						for (VTTunnelChannelSocketListener channel : channels)
						{
							message.append("\nVT>Client SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
						}
						message.append("\nVT>End of client connection SOCKS tunnels list\nVT>");
						VTConsole.print(message.toString());
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
					}
				}
				else if (splitCommand.length == 3)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
								if (channel != null)
								{
									channel.close();
									session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel("", bindPort);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length == 4)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
								if (channel != null)
								{
									channel.close();
									session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							//String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand.length == 5)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							String socksUsername = splitCommand[3];
							String socksPassword = splitCommand[4];
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
								if (channel != null)
								{
									channel.close();
									session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel("", bindPort, socksUsername, socksPassword);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " "  + bindPort + "] set!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							int bindPort = Integer.parseInt(splitCommand[2]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
				}
				else if (splitCommand.length >= 6)
				{
					if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						try
						{
							String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							String socksUsername = splitCommand[4];
							String socksPassword = splitCommand[5];
							
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
								if (channel != null)
								{
									channel.close();
									session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
								}
								else
								{
									session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort, socksUsername, socksPassword);
									VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + bindAddress + " "  + bindPort + "] set!\nVT>");
								}
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
					else if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						try
						{
							//String bindAddress = splitCommand[2];
							int bindPort = Integer.parseInt(splitCommand[3]);
							if (bindPort < 1 || bindPort > 65535)
							{
								VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
							}
							else
							{
								connection.getCommandWriter().write(command + "\n");
								connection.getCommandWriter().flush();
							}
						}
						catch (IndexOutOfBoundsException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
						catch (NumberFormatException e)
						{
							VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						}
					}
				}
				else
				{
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTAUDIOLINK") || splitCommand[0].equalsIgnoreCase("*VTAL"))
			{
				if (session.isRunningAudio())
				{
					// session.getClient().getAudioSystem().stop();
					session.setRunningAudio(false);
					connection.closeAudioStreams();
					session.getClient().getAudioSystem().stop();
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
				else
				{
					// session.getClient().getAudioSystem().stop();
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
										try
										{
											int index = Integer.parseInt(mixer);
											inputMixer = info[index];
										}
										catch (Throwable t)
										{
											VTConsole.print("\nVT>Invalid local input audio mixer number [" + mixer + "]!");
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
											VTConsole.print("\nVT>Invalid local output audio mixer number [" + mixer + "]!");
											// ok = false;
										}
									}
									else
									{
										// ok = false;
										// VTConsole.print("\nVT>Invalid
										// command syntax!" +
										// VTHelpManager.getHelpForClientCommand(splitCommand[0]));
										// break;
									}
								}
								else if (side.toUpperCase().startsWith("R"))
								{
									if (type.toUpperCase().startsWith("I"))
									{
										try
										{
											Integer.parseInt(mixer);
										}
										catch (Throwable t)
										{
											// VTConsole.print("\nVT>Invalid
											// remote audio mixer number [" +
											// mixer + "]!");
											// ok = false;
										}
									}
									else if (type.toUpperCase().startsWith("O"))
									{
										try
										{
											Integer.parseInt(mixer);
										}
										catch (Throwable t)
										{
											// VTConsole.print("\nVT>Invalid
											// remote audio mixer number [" +
											// mixer + "]!");
											// ok = false;
										}
									}
									else
									{
										// ok = false;
										// VTConsole.print("\nVT>Invalid
										// command syntax!" +
										// VTHelpManager.getHelpForClientCommand(splitCommand[0]));
										// break;
									}
								}
								else
								{
									// ok = false;
									// VTConsole.print("\nVT>Invalid command
									// syntax!" +
									// VTHelpManager.getHelpForClientCommand(splitCommand[0]));
									// break;
								}
							}
							else
							{
								// ok = false;
								// VTConsole.print("\nVT>Invalid command
								// syntax!" +
								// VTHelpManager.getHelpForClientCommand(splitCommand[0]));
								// break;
							}
						}
					}
					
					connection.resetAudioStreams();
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
					session.getClient().getAudioSystem().initialize(VT.VT_AUDIO_FORMAT);
					if (connection.getAudioControlInputStream().read() == 1)
					{
						if (session.getClient().getAudioSystem().addAudioPlay(connection.getAudioDataInputStream(), outputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
						{
							connection.getAudioControlOutputStream().write(1);
							connection.getAudioControlOutputStream().flush();
							if (connection.getAudioControlInputStream().read() == 1)
							{
								if (session.getClient().getAudioSystem().addAudioCapture(connection.getAudioDataOutputStream(), inputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
								{
									connection.getAudioControlOutputStream().write(1);
									connection.getAudioControlOutputStream().flush();
									session.setRunningAudio(true);
								}
								else
								{
									connection.getAudioControlOutputStream().write(0);
									connection.getAudioControlOutputStream().flush();
									connection.closeAudioStreams();
									session.setRunningAudio(false);
									session.getClient().getAudioSystem().stop();
									// session.getClient().getAudioSystem().stop();
									VTConsole.print("\nVT>Remote audio link start on client failed!\nVT>");
								}
							}
							else
							{
								connection.closeAudioStreams();
								session.setRunningAudio(false);
								session.getClient().getAudioSystem().stop();
							}
						}
						else
						{
							connection.getAudioControlOutputStream().write(0);
							connection.getAudioControlOutputStream().flush();
							connection.closeAudioStreams();
							session.setRunningAudio(false);
							session.getClient().getAudioSystem().stop();
							// session.getClient().getAudioSystem().stop();
							VTConsole.print("\nVT>Remote audio link start on client failed!\nVT>");
						}
					}
					else
					{
						connection.closeAudioStreams();
						session.setRunningAudio(false);
						session.getClient().getAudioSystem().stop();
						// session.getClient().getAudioSystem().stop();
						// VTConsole.print("\nVT>Remote audio link start on
						// client failed!\nVT>");
					}
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTPING") || splitCommand[0].equalsIgnoreCase("*VTPG"))
			{
				session.getNanoPingService().ping();
				// connection.getCommandWriter().write(command + "\n");
				// connection.getCommandWriter().flush();
				long clientTime = session.getLocalNanoDelay();
				long serverTime = session.getRemoteNanoDelay();
				long nanoseconds = ((clientTime + serverTime) / 2);
				long millisseconds = ((clientTime + serverTime) / 2) / 1000000;
				//long estimated = ((clientTime + serverTime) / 2);
				// VTTerminal.printf("\nVT>Current client/server network
				// connection latency: %.2f ms\nVT>", estimated);
				VTConsole.printf("\nVT>Estimated connection latency: [%d] ms or [%d] ns\nVT>", millisseconds, nanoseconds);
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTAUDIOMIXERS") || splitCommand[0].equalsIgnoreCase("*VTAM"))
			{
				if (splitCommand.length >= 2)
				{
					if (splitCommand[1].toUpperCase().startsWith("R"))
					{
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
					}
					else if (splitCommand[1].toUpperCase().startsWith("L"))
					{
						message.setLength(0);
						message.append("\nVT>List of local audio mixers:\nVT>");
						Mixer.Info[] mixers = AudioSystem.getMixerInfo();
						int number = 0;
						for (Mixer.Info info : mixers)
						{
							message.append("\nVT>Number: [" + number++ + "]");
							message.append("\nVT>Name: [" + info.getName() + "]");
							message.append("\nVT>Description: [" + info.getDescription() + "]");
							message.append("\nVT>");
						}
						message.append("\nVT>End of local audio mixers list\nVT>");
						VTConsole.print(message.toString());
					}
					else
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else
				{
					message.setLength(0);
					message.append("\nVT>List of local audio mixers:\nVT>");
					Mixer.Info[] mixers = AudioSystem.getMixerInfo();
					int number = 0;
					for (Mixer.Info info : mixers)
					{
						message.append("\nVT>Number: [" + number++ + "]");
						message.append("\nVT>Name: [" + info.getName() + "]");
						message.append("\nVT>Description: [" + info.getDescription() + "]");
						message.append("\nVT>");
					}
					message.append("\nVT>End of local audio mixers list\nVT>");
					VTConsole.print(message.toString());
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
				}
			}
			else if (splitCommand[0].equalsIgnoreCase("*VTRATELIMIT") || splitCommand[0].equalsIgnoreCase("*VTRL"))
			{
				if (splitCommand.length == 1)
				{
					long rate = connection.getRateInBytesPerSecond();
					if (rate > 0)
					{
						VTConsole.print("\nVT>Connection upload rate limit: [" + rate + "] bytes per second\nVT>");
					}
					else
					{
						VTConsole.print("\nVT>Connection upload rate limit: [Unlimited] bytes per second\nVT>");
					}
					connection.getCommandWriter().write(command + "\n");
					connection.getCommandWriter().flush();
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
							VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
						}
						else
						{
							VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
						}
						connection.getCommandWriter().write(command + "\n");
						connection.getCommandWriter().flush();
					}
					catch (NumberFormatException e)
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
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
						if (splitCommand[2].toUpperCase().startsWith("D"))
						{
							connection.getCommandWriter().write(command + "\n");
							connection.getCommandWriter().flush();
						}
						else
						{
							connection.setRateInBytesPerSecond(rate);
							if (rate > 0)
							{
								VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
							}
							else
							{
								VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
							}
							connection.getCommandWriter().write(command + "\n");
							connection.getCommandWriter().flush();
						}
					}
					catch (NumberFormatException e)
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					}
				}
				else if (splitCommand[0].equalsIgnoreCase("*VTSAVE") || splitCommand[0].equalsIgnoreCase("*VTSV"))
				{
					if (splitCommand.length == 1)
					{
						try
						{
							session.getClient().saveClientSettingsFile("variable-terminal-client.properties");
							VTConsole.print("\nVT>Saved client settings file [variable-terminal-client.properties]\nVT>");
						}
						catch (Throwable t)
						{
							VTConsole.print("\nVT>Cannot save client settings file [variable-terminal-client.properties]\nVT>");
						}
					}
					else if (splitCommand.length >= 2)
					{
						try
						{
							session.getClient().saveClientSettingsFile(splitCommand[1]);
							VTConsole.print("\nVT>Saved client settings file [" + splitCommand[1] + "]\nVT>");
						}
						catch (Throwable t)
						{
							VTConsole.print("\nVT>Cannot save client settings file [" + splitCommand[1] + "]\nVT>");
						}
					}
				}
			}
			else
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
			}
		}
		else
		{
			System.exit(0);
		}
	}
}