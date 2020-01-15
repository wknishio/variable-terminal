package org.vate.server.console.remote.standard.command;

import java.util.Set;

import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTCPTUNNEL extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTTCPTUNNEL()
	{
		this.setFullName("*VTTCPTUNNEL");
		this.setAbbreviatedName("*VTTTN");
		this.setFullSyntax("*VTTCPTUNNEL [SIDE] [[BIND] PORT] [[HOST] PORT]|");
		this.setAbbreviatedSyntax("*VTTTN [SD] [[BD] PT] [[HT] PT]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length == 1)
		{
			message.setLength(0);
			for (VTTunnelChannelSocketListener channel : session.getTCPTunnelsHandler().getConnection().getChannels())
			{
				message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]"
				+ "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]"
				+ "\nVT>");
			}
			message.append("\nVT>End of connection TCP tunnels list\nVT>");
			connection.getResultWriter().write(message.toString());
			connection.getResultWriter().flush();
		}
		else if (parsed.length == 2)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsHandler().getConnection().getChannels();
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
		else if (parsed.length == 3)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				try
				{
					int bindPort = Integer.parseInt(parsed[2]);
					VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
					if (channel != null)
					{
						channel.close();
						session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
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
		else if (parsed.length == 4)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				try
				{
					int bindPort = Integer.parseInt(parsed[2]);
					int redirectPort = Integer.parseInt(parsed[3]);
					if (bindPort < 1 || bindPort > 65535)
					{
						
					}
					else
					{
						session.getTCPTunnelsHandler().getConnection().setTCPChannel("", bindPort, "", redirectPort);
						connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				catch (NumberFormatException e1)
				{
					try
					{
						String redirectAddress = parsed[2];
						int bindPort = Integer.parseInt(parsed[3]);
						
						VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener(redirectAddress, bindPort);
						if (channel != null)
						{
							channel.close();
							session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
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
		else if (parsed.length == 5)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				try
				{
					int bindPort = Integer.parseInt(parsed[2]);
					String redirectAddress = parsed[3];
					int redirectPort = Integer.parseInt(parsed[4]);
					
					if (session.getTCPTunnelsHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
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
						String bindAddress = parsed[2];
						int bindPort = Integer.parseInt(parsed[3]);
						int redirectPort = Integer.parseInt(parsed[4]);
						
						if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
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
		else if (parsed.length >= 6)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				try
				{
					String bindAddress = parsed[2];
					int bindPort = Integer.parseInt(parsed[3]);
					String redirectAddress = parsed[4];
					int redirectPort = Integer.parseInt(parsed[5]);
					
					if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
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

	public void close()
	{
		
	}
}
