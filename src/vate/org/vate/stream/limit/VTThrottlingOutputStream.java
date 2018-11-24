package org.vate.stream.limit;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTThrottlingOutputStream extends FilterOutputStream
{
	private VTThrottledOutputStream throttled;
	private OutputStream original;
	private OutputStream current;
	private long bytesPerSecond;
	
	public VTThrottlingOutputStream(OutputStream out)
	{
		super(out);
		this.throttled = new VTThrottledOutputStream(out, 1);
		this.original = out;
		this.current = out;
		this.bytesPerSecond = 0;
	}
	
	public void write(int b) throws IOException
	{
		// System.out.println("write:" + current.getClass().getName());
		current.write(b);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		// System.out.println("write:" + current.getClass().getName());
		current.write(b, off, len);
	}
	
	public void setBytesPerSecond(long bytesPerSecond)
	{
		this.bytesPerSecond = bytesPerSecond;
		if (bytesPerSecond > 0)
		{
			throttled.setBytesPerSecond(bytesPerSecond);
			throttled.wakeAllWaitingThreads();
			current = throttled;
		}
		else
		{
			throttled.setBytesPerSecond(Long.MAX_VALUE);
			throttled.wakeAllWaitingThreads();
			current = original;
		}
	}
	
	public long getBytesPerSecond()
	{
		return bytesPerSecond;
	}
	
	public void close() throws IOException
	{
		throttled.setBytesPerSecond(Long.MAX_VALUE);
		throttled.wakeAllWaitingThreads();
		current = original;
		out.close();
	}
}