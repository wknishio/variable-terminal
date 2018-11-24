package org.vate.stream.array;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VTByteArrayOutputStream extends ByteArrayOutputStream
{
	public VTByteArrayOutputStream()
	{
		super();
	}
	
	public VTByteArrayOutputStream(int size)
	{
		super(size);
	}
	
	public byte[] buf()
	{
		return this.buf;
	}
	
	public int count()
	{
		return this.count;
	}
	
	public void buf(byte[] buf)
	{
		this.buf = buf;
	}
	
	public void count(int count)
	{
		this.count = count;
	}
	
	public void close() throws IOException
	{
		// super.close();
	}
}