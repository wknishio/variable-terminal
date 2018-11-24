package org.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import engineering.clientside.throttle.NanoThrottle;

public class VTThrottledOutputStream extends FilterOutputStream
{
	private volatile NanoThrottle throttler;
	
	public VTThrottledOutputStream(OutputStream out, double bytesPerSecond)
	{
		super(out);
		this.throttler = new NanoThrottle(bytesPerSecond, 0.01, true);
	}
	
	public void write(int b) throws IOException
	{
		try
		{
			throttler.acquire(1);
		}
		catch (InterruptedException e)
		{
			
		}
		out.write(b);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		try
		{
			throttler.acquire(len);
		}
		catch (InterruptedException e)
		{
			
		}
		out.write(b, off, len);
	}
	
	public void setBytesPerSecond(long bytesPerSecond)
	{
		throttler.setRate(bytesPerSecond);
	}
	
	public double getBytesPerSecond()
	{
		return throttler.getRate();
	}
	
	public void wakeAllWaitingThreads()
	{
		throttler.wakeAllWaitingThreads();
	}
	
	public void close() throws IOException
	{
		throttler.setRate(Long.MAX_VALUE);
		throttler.wakeAllWaitingThreads();
		super.close();
	}
}