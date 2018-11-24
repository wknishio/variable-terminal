package org.vate.stream.array;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

public class VTByteArrayInputStream extends ByteArrayInputStream
{
	public VTByteArrayInputStream(byte[] buf)
	{
		super(buf);
	}
	
	public VTByteArrayInputStream(byte[] buf, int offset, int length)
	{
		super(buf, offset, length);
	}
	
	public byte[] buf()
	{
		return this.buf;
	}
	
	public int count()
	{
		return this.count;
	}
	
	public int mark()
	{
		return this.mark;
	}
	
	public int pos()
	{
		return this.pos;
	}
	
	public void buf(byte[] buf)
	{
		this.buf = buf;
	}
	
	public void count(int count)
	{
		this.count = count;
	}
	
	public void pos(int pos)
	{
		this.pos = pos;
	}
	
	public int readByte() throws IOException
	{
		int b = read();
		if (b == -1)
		{
			throw new EOFException();
		}
		return b;
	}
	
}