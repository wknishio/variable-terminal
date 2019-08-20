package org.vate.server.console.local.standard.command;

import java.util.List;

import org.vate.console.VTConsole;
import org.vate.server.connection.VTServerConnectionHandler;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTMESSAGE extends VTServerStandardLocalConsoleCommandProcessor
{	
	public VTMESSAGE()
	{
		this.setFullName("*VTMESSAGE");
		this.setAbbreviatedName("*VTMSG");
		this.setFullSyntax("*VTMESSAGE <MESSAGE>");
		this.setAbbreviatedSyntax("*VTMSG <MSG>");
	}

	public void execute(String command, String[] parsed) throws Exception
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

	public void close()
	{
		
	}
}
