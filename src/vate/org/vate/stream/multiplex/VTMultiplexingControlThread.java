package org.vate.stream.multiplex;

import java.util.concurrent.ExecutorService;

public class VTMultiplexingControlThread implements Runnable
{
	private VTMultiplexingConnection connection;
	private ExecutorService threads;
	// private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;
	
	public VTMultiplexingControlThread(VTMultiplexingConnection connection, ExecutorService threads)
	{
		this.connection = connection;
		this.threads = threads;
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				String line = connection.getControlReader().readLine();
				if (line != null && line.length() > 0)
				{
					threads.hashCode();
				}
			}
			catch (Throwable e)
			{
				//e.printStackTrace();
				return;
			}
		}
	}
}