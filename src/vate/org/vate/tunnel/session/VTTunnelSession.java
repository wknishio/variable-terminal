package org.vate.tunnel.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vate.tunnel.connection.VTTunnelConnection;

public class VTTunnelSession
{
	private VTTunnelConnection connection;
	private Socket socket;
	private InputStream socketInputStream;
	private OutputStream socketOutputStream;
	private VTLinkableDynamicMultiplexedInputStream inputStream;
	private VTLinkableDynamicMultiplexedOutputStream outputStream;
	private final boolean originator;
	
	public VTTunnelSession(VTTunnelConnection connection, Socket socket) throws IOException
	{
		this.connection = connection;
		this.socket = socket;
		this.socketInputStream = socket.getInputStream();
		this.socketOutputStream = socket.getOutputStream();
		this.originator = true;
	}
	
	public VTTunnelSession(VTTunnelConnection connection, VTLinkableDynamicMultiplexedInputStream inputStream)
	{
		this.connection = connection;
		this.inputStream = inputStream;
		this.originator = false;
	}
	
	public boolean isOriginator()
	{
		return originator;
	}
	
	/* public boolean isReady() { return ready; } */
	
	/* public void setReady(boolean ready) { this.ready = ready; } */
	
	/* public void linger() { if (socket != null) { try {
	 * socket.setSoLinger(true, 0); } catch (Throwable e) {
	 * } } } */
	
	public void close() throws IOException
	{
		try
		{
			if (outputStream != null)
			{
				outputStream.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			if (socket != null)
			{
				socket.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			if (socketOutputStream != null)
			{
				socketOutputStream.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			if (socketInputStream != null)
			{
				socketInputStream.close();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		connection.releaseOutputStream(outputStream);
	}
	
	public void setSocket(Socket socket) throws IOException
	{
		this.socket = socket;
		this.socketInputStream = socket.getInputStream();
		this.socketOutputStream = socket.getOutputStream();
	}
	
	public Socket getSocket()
	{
		return this.socket;
	}
	
	public InputStream getSocketInputStream()
	{
		return socketInputStream;
	}
	
	public OutputStream getSocketOutputStream()
	{
		return socketOutputStream;
	}
	
	public VTLinkableDynamicMultiplexedInputStream getTunnelInputStream()
	{
		return inputStream;
	}
	
	public void setTunnelInputStream(VTLinkableDynamicMultiplexedInputStream inputStream)
	{
		this.inputStream = inputStream;
	}
	
	public VTLinkableDynamicMultiplexedOutputStream getTunnelOutputStream()
	{
		return outputStream;
	}
	
	public void setTunnelOutputStream(VTLinkableDynamicMultiplexedOutputStream outputStream)
	{
		this.outputStream = outputStream;
	}
}