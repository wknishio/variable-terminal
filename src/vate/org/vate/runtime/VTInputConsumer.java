package org.vate.runtime;

import java.io.InputStream;

public class VTInputConsumer implements Runnable
{
	private final byte[] buffer = new byte[1024 * 8];
	private InputStream in;
	
	public VTInputConsumer(InputStream in)
	{
		this.in = in;
	}
	
	public void close()
	{
		try
		{
			in.close();
		}
		catch (Throwable e)
		{
			
		}
	}
	
	public void run()
	{
		try
		{
			int readed = 1;
			while (readed > 0)
			{
				readed = in.read(buffer, 0, buffer.length);
			}
		}
		catch (Throwable e)
		{
			
		}
		try
		{
			in.close();
		}
		catch (Throwable e)
		{
			
		}
	}
}