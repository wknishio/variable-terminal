package org.vate.tunnel.channel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vate.tunnel.connection.VTTunnelConnection;
import org.vate.tunnel.session.VTTunnelSession;
import org.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelSocketListener implements Runnable
{
	private ServerSocket serverSocket;
	private VTTunnelChannel channel;
	
	public VTTunnelChannelSocketListener(VTTunnelChannel channel)
	{
		try
		{
			this.serverSocket = new ServerSocket();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		this.channel = channel;
	}
	
	public String toString()
	{
		return channel.toString();
	}
	
	public boolean equals(Object other)
	{
		return this.toString().equals(other.toString());
	}
	
	public VTTunnelChannel getChannel()
	{
		return channel;
	}
	
	public void close() throws IOException
	{
		try
		{
			if (serverSocket != null)
			{
				serverSocket.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			channel.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		// channel.getConnection().removeChannel(this);
	}
	
	public void remove()
	{
		channel.getConnection().removeChannel(this);
	}
	
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			while (!serverSocket.isClosed() && !serverSocket.isBound())
			{
				try
				{
					serverSocket.setReuseAddress(true);
					serverSocket.bind(channel.getBindAddress());
				}
				catch (Throwable e)
				{
					// e.printStackTrace();
					Thread.sleep(1000);
				}
			}
			while (!serverSocket.isClosed() && serverSocket.isBound())
			{
				Socket socket = null;
				try
				{
					socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
					socket.setKeepAlive(true);
					//socket.setSoLinger(true, 0);
					VTTunnelSession session = new VTTunnelSession(channel.getConnection(), socket);
					VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, channel);
					VTLinkableDynamicMultiplexedOutputStream stream = channel.getConnection().getOutputStream(handler);
					if (stream != null)
					{
						int number = stream.number();
						session.setTunnelOutputStream(stream);
						session.getTunnelOutputStream().open();
						session.setTunnelInputStream(channel.getConnection().getInputStream(number));
						session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream());
						if (channel.getConnection().getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_TCP)
						{
							String host = channel.getRedirectHost();
							int port = channel.getRedirectPort();
							//first message sent
							channel.getConnection().getControlWriter().write(number + ";:;" + host + ";:;" + port + "\n");
							channel.getConnection().getControlWriter().flush();
						}
						else if (channel.getConnection().getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_SOCKS)
						{
							String socksUsername = channel.getSocksUsername();
							String socksPassword = channel.getSocksPassword();
							if (socksUsername == null)
							{
								socksUsername = "*";
								socksPassword = "*;:;*";
							}
							//first message sent
							channel.getConnection().getControlWriter().write(number + ";:;" + socksUsername + ";:;" + socksPassword + "\n");
							channel.getConnection().getControlWriter().flush();
						}
						// System.out.println(session.getOutputStream().number()
						// + " " + host + " " +
						// port);
					}
					else
					{
						//cannot handle more sessions
						session.close();
					}
				}
				catch (Throwable e)
				{
					if (socket != null)
					{
						socket.close();
					}
					// e.printStackTrace();
				}
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
}