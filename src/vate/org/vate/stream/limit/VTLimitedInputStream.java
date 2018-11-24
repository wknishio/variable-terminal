package org.vate.stream.limit;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VTLimitedInputStream extends FilterInputStream
{
	private int limit = 0;
	// private long skipped = 0;
	
	public VTLimitedInputStream(InputStream in)
	{
		super(in);
	}
	
	public void setLimit(int limit)
	{
		this.limit = limit;
	}
	
	public int available() throws IOException
	{
		int available = in.available();
		return Math.min(limit, available);
	}
	
	public void empty() throws IOException
	{
		long skipped = 0;
		while (limit > 0 && skipped >= 0)
		{
			skipped = in.skip(limit);
			if (skipped < 0)
			{
				limit = -1;
				return;
			}
			limit -= skipped;
		}
	}
	
	public int read() throws IOException
	{
		if (limit <= 0)
		{
			return -1;
		}
		else
		{
			limit--;
		}
		return in.read();
	}
	
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (limit <= 0)
		{
			return -1;
		}
		else
		{
			if (len == 0)
			{
				return 0;
			}
			int readed = in.read(b, off, Math.min(limit, len));
			limit -= readed;
			return readed;
		}
	}
	
	public void close() throws IOException
	{
		
	}
	
	public void forceClose() throws IOException
	{
		in.close();
	}
}