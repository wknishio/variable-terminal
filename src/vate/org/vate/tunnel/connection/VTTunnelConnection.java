package org.vate.tunnel.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.vate.VT;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vate.tunnel.channel.VTTunnelChannel;
import org.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTunnelConnection
{
	public static final int TUNNEL_TYPE_TCP = 0;
	public static final int TUNNEL_TYPE_SOCKS = 1;
	
	private VTLinkableDynamicMultiplexingInputStream dataInputStream;
	private VTLinkableDynamicMultiplexingOutputStream dataOutputStream;
	private BufferedReader controlReader;
	private BufferedWriter controlWriter;
	private Set<VTTunnelChannelSocketListener> channels;
	private int tunnelType;
	private ExecutorService threads;
	
	public VTTunnelConnection(int tunnelType, ExecutorService threads)
	{
		this.channels = new LinkedHashSet<VTTunnelChannelSocketListener>();
		this.tunnelType = tunnelType;
		this.threads = threads;
	}
	
	public int getTunnelType()
	{
		return tunnelType;
	}
	
	public synchronized boolean setSOCKSChannel(String bindHost, int bindPort)
	{
		VTTunnelChannelSocketListener listener = getChannelSocketListener(bindHost, bindPort);
		if (listener != null)
		{
			return true;
		}
		VTTunnelChannel channel = new VTTunnelChannel(this, bindHost, bindPort);
		listener = new VTTunnelChannelSocketListener(channel);
		channels.add(listener);
		threads.execute(listener);
		// Thread listenerThread = new Thread(listener,
		// listener.getClass().getSimpleName());
		// listenerThread.start();
		return true;
	}
	
	public synchronized boolean setSOCKSChannel(String bindHost, int bindPort, String socksUsername, String socksPassword)
	{
		VTTunnelChannelSocketListener listener = getChannelSocketListener(bindHost, bindPort);
		if (listener != null)
		{
			if (socksUsername.equals(listener.getChannel().getSocksUsername()) && socksPassword.equals(listener.getChannel().getSocksPassword()))
			{
				return false;
			}
			listener.getChannel().setSocksUsername(socksUsername);
			listener.getChannel().setSocksPassword(socksPassword);
			return true;
		}
		VTTunnelChannel channel = new VTTunnelChannel(this, bindHost, bindPort, socksUsername, socksPassword);
		listener = new VTTunnelChannelSocketListener(channel);
		channels.add(listener);
		// Thread listenerThread = new Thread(listener,
		// listener.getClass().getSimpleName());
		// listenerThread.start();
		threads.execute(listener);
		return true;
	}
	
	public synchronized boolean setTCPChannel(String bindHost, int bindPort, String redirectHost, int redirectPort)
	{
		VTTunnelChannelSocketListener listener = getChannelSocketListener(bindHost, bindPort);
		if (listener != null)
		{
			String currentRedirectHost = listener.getChannel().getRedirectHost();
			int currentRedirectPort = listener.getChannel().getRedirectPort();
		
			if (currentRedirectHost.equals(redirectHost) && currentRedirectPort == redirectPort)
			{
				return false;
			}
			listener.getChannel().setRedirectAddress(redirectHost, redirectPort);
			return true;
		}
		VTTunnelChannel channel = new VTTunnelChannel(this, bindHost, bindPort, redirectHost, redirectPort);
		listener = new VTTunnelChannelSocketListener(channel);
		channels.add(listener);
		// Thread listenerThread = new Thread(listener,
		// listener.getClass().getSimpleName());
		// listenerThread.start();
		threads.execute(listener);
		return true;
	}
	
	public synchronized Set<VTTunnelChannelSocketListener> getChannels()
	{
		return channels;
	}
	
	public VTTunnelChannelSocketListener getChannelSocketListener(String bindHost, int bindPort)
	{
		if (bindHost == null || bindHost.length() == 0)
		{
			for (VTTunnelChannelSocketListener channel : channels)
			{
				if (channel.getChannel().getBindPort() == bindPort)
				{
					InetAddress bindAddress = channel.getChannel().getBindAddress().getAddress();
					if (bindAddress != null && bindAddress.isAnyLocalAddress())
					{
						return channel;
					}
				}
			}
		}
		else
		{
			for (VTTunnelChannelSocketListener channel : channels)
			{
				if (channel.getChannel().getBindPort() == bindPort)
				{
					InetAddress bindAddress = channel.getChannel().getBindAddress().getAddress();
					if (bindAddress != null)
					{
						if (bindHost.equals(bindAddress.getHostAddress())
						|| bindHost.equals(bindAddress.getHostName()))
						{
							return channel;
						}
					}
				}
			}
		}
		return null;
	}
	
	public synchronized boolean removeChannel(VTTunnelChannelSocketListener listener)
	{
		return channels.remove(listener);
	}
	
	/* public OutputStream getDataOutputStream() { return dataOutputStream; } */
	
	public synchronized void start()
	{
		//dataInputStream.startPacketReader();
	}
	
	public synchronized void stop()
	{
		try
		{
			//dataInputStream.stopPacketReader();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
	
	public synchronized void close()
	{
		//stop();
		for (VTTunnelChannelSocketListener listener : channels)
		{
			try
			{
				listener.close();
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
			}
		}
		channels.clear();
		try
		{
			controlReader.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			controlWriter.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			//dataInputStream.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			//dataOutputStream.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
	
	public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(Object link)
	{
		if (link instanceof Integer)
		{
			return dataOutputStream.getOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT/*|VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED*/), (Integer) link);
		}
		return dataOutputStream.linkOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT/*|VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED*/), link);
	}
	
	public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number, Object link)
	{
		VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.getOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT/*|VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED*/), number);
		stream.setLink(link);
		return stream;
	}
	
	public synchronized void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
	{
		if (stream != null)
		{
			dataOutputStream.releaseOutputStream(stream);
		}
	}
	
	public synchronized VTLinkableDynamicMultiplexedInputStream getInputStream(int number)
	{
		VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.getInputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT/*|VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED*/), number);
		return stream;
	}
	
	public BufferedReader getControlReader()
	{
		return controlReader;
	}
	
	public BufferedWriter getControlWriter()
	{
		return controlWriter;
	}
	
	public void setDataInputStream(VTLinkableDynamicMultiplexingInputStream in)
	{
		this.dataInputStream = in;
	}
	
	public void setControlInputStream(InputStream in)
	{
		controlReader = new BufferedReader(new InputStreamReader(in));
	}
	
	public void setDataOutputStream(VTLinkableDynamicMultiplexingOutputStream out)
	{
		this.dataOutputStream = out;
	}
	
	public void setControlOutputStream(OutputStream out)
	{
		controlWriter = new BufferedWriter(new OutputStreamWriter(out));
	}
}