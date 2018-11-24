package org.vate.tunnel.connection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vate.tunnel.session.VTTunnelSession;
import org.vate.tunnel.session.VTTunnelSessionHandler;
import org.vate.tunnel.session.VTTunnelSocksSessionHandler;
import org.vate.tunnel.session.VTTunnelVirtualSocket;

public class VTTunnelConnectionControlThread implements Runnable
{
	private VTTunnelConnection connection;
	private ExecutorService threads;
	// private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;
	
	public VTTunnelConnectionControlThread(VTTunnelConnection connection, ExecutorService threads)
	{
		this.connection = connection;
		this.threads = threads;
	}
	
	/* public void setTunnelType(int tunnelType) { this.tunnelType = tunnelType;
	 * } */
	
	public void run()
	{
		while (true)
		{
			try
			{
				String line = connection.getControlReader().readLine();
				// System.out.println(line);
				String[] data = line.split(";:;");
				if (data.length >= 3)
				{
					if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_TCP)
					{
						int input = Integer.parseInt(data[0]);
						String host = data[1];
						int port = Integer.parseInt(data[2]);
						VTTunnelSession session = new VTTunnelSession(connection, connection.getInputStream(input));
						VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, null);
						try
						{
							// server 2
							Socket socket = new Socket();
							socket.connect(new InetSocketAddress(host, port));
							socket.setTcpNoDelay(true);
							socket.setKeepAlive(true);
							socket.setSoLinger(true, 0);
							session.setSocket(socket);
							VTLinkableDynamicMultiplexedOutputStream stream = connection.getOutputStream(handler);
							if (stream != null)
							{
								session.setTunnelOutputStream(stream);
								session.getTunnelOutputStream().open();
								session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream());
								connection.getControlWriter().write(input + ";:;" + session.getTunnelOutputStream().number() + "\n");
								connection.getControlWriter().flush();
							}
							else
							{
								session.close();
								connection.getControlWriter().write(input + ";:;-1\n");
								connection.getControlWriter().flush();
							}
						}
						catch (Throwable e)
						{
							session.close();
							connection.getControlWriter().write(input + ";:;-1\n");
							connection.getControlWriter().flush();
						}
					}
					else if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_SOCKS)
					{
						int input = Integer.parseInt(data[0]);
						String socksUsername = data[1];
						String socksPassword = data[2];
						VTTunnelSession session = new VTTunnelSession(connection, connection.getInputStream(input));
						VTTunnelSocksSessionHandler handler = null;
						if (data.length == 4)
						{
							handler = new VTTunnelSocksSessionHandler(session, null);
						}
						else
						{
							handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword);
						}
						try
						{
							// server 2
							VTLinkableDynamicMultiplexedOutputStream stream = connection.getOutputStream(handler);
							VTTunnelVirtualSocket socket = new VTTunnelVirtualSocket(stream);
							session.setSocket(socket);
							
							if (stream != null)
							{
								session.setTunnelOutputStream(stream);
								session.getTunnelOutputStream().open();
								session.getTunnelInputStream().setOutputStream(socket.getInputStreamSource());
								connection.getControlWriter().write(input + ";:;" + session.getTunnelOutputStream().number() + "\n");
								connection.getControlWriter().flush();
							}
							else
							{
								session.close();
								connection.getControlWriter().write(input + ";:;-1\n");
								connection.getControlWriter().flush();
							}
						}
						catch (Throwable e)
						{
							session.close();
							connection.getControlWriter().write(input + ";:;-1\n");
							connection.getControlWriter().flush();
						}
					}
				}
				else if (data.length == 2)
				{
					int output = Integer.parseInt(data[0]);
					int input = Integer.parseInt(data[1]);
					VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(output).getLink());
					VTTunnelSession session = handler.getSession();
					if (input > -1)
					{
						if (session.getTunnelInputStream() == null)
						{
							// client 3
							session.setTunnelInputStream(connection.getInputStream(input));
							session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream());
							connection.getControlWriter().write(input + ";:;" + output + "\n");
							connection.getControlWriter().flush();
						}
						else
						{
							if (session.isOriginator())
							{
								// client 5
								threads.execute(handler);
								// Thread handlerThread = new Thread(handler,
								// handler.getClass().getSimpleName());
								// handlerThread.start();
							}
							else
							{
								// server 4
								connection.getControlWriter().write(input + ";:;" + output + "\n");
								connection.getControlWriter().flush();
								threads.execute(handler);
								// Thread handlerThread = new Thread(handler,
								// handler.getClass().getSimpleName());
								// handlerThread.start();
							}
						}
					}
					else
					{
						// client 3
						session.close();
					}
				}
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
				return;
			}
		}
	}
}