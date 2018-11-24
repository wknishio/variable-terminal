package org.vate.stream.pipe;

import java.io.InputStream;
import java.io.OutputStream;

public class VTInterruptibleStreamRedirector implements Runnable
{
	private static final int redirectorBufferSize = 1024 * 8;
	private volatile boolean stopped;
	private int available;
	private int readed;
	private final byte[] redirectorBuffer = new byte[redirectorBufferSize];
	private InputStream source;
	private OutputStream destination;
	// private VTTunnelSession session;
	
	public VTInterruptibleStreamRedirector(InputStream source, OutputStream destination)
	{
		this.source = source;
		this.destination = destination;
	}
	
	public void run()
	{
		while (!stopped)
		{
			try
			{
				available = source.available();
				if (available > 0)
				{
					available = Math.min(available, redirectorBufferSize);
					readed = source.read(redirectorBuffer, 0, available);
					if (readed > 0)
					{
						destination.write(redirectorBuffer, 0, readed);
						destination.flush();
					}
					else if (readed < 0)
					{
						stopped = true;
						break;
					}
				}
				else
				{
					Thread.sleep(1);
				}
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
		}
	}
}