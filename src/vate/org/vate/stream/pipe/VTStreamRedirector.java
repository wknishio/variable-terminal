package org.vate.stream.pipe;

import java.io.InputStream;
import java.io.OutputStream;

public class VTStreamRedirector implements Runnable
{
	private static final int redirectorBufferSize = 1024 * 64;
	private volatile boolean stopped;
	private int readed = 0;
	private final byte[] redirectorBuffer = new byte[redirectorBufferSize];
	private InputStream source;
	private OutputStream destination;
	// private VTTunnelSession session;
	
	public VTStreamRedirector(InputStream source, OutputStream destination)
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
				readed = source.read(redirectorBuffer, 0, redirectorBufferSize);
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
			catch (Throwable e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
		}
	}
}