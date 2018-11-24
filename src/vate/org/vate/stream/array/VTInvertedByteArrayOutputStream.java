package org.vate.stream.array;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTInvertedByteArrayOutputStream extends ByteArrayOutputStream
{
	// private int end = 0;
	
	public VTInvertedByteArrayOutputStream()
	{
		super();
		// end = buf.length;
	}
	
	public VTInvertedByteArrayOutputStream(int size)
	{
		super(size);
		// end = buf.length - 1;
	}
	
	public synchronized byte[] buf()
	{
		return this.buf;
	}
	
	public synchronized int count()
	{
		return this.count;
	}
	
	public synchronized void buf(byte[] buf)
	{
		this.buf = buf;
	}
	
	public synchronized void count(int count)
	{
		this.count = count;
	}
	
	public synchronized void write(int b)
	{
		int newcount = count + 1;
		if (newcount > buf.length)
		{
			byte[] newbuf = new byte[buf.length << 1];
			System.arraycopy(buf, 0, newbuf, buf.length, buf.length);
			buf = newbuf;
		}
		count = newcount;
		buf[buf.length - count] = (byte) b;
	}
	
	public synchronized void write(byte[] data, int off, int len)
	{
		for (int i = 0; i < len; i++)
		{
			write(data[off + i]);
		}
	}
	
	public synchronized byte[] toByteArray()
	{
		byte[] data = new byte[count];
		System.arraycopy(buf, buf.length - count, data, 0, count);
		return data;
		// return Arrays.copyOfRange(buf, buf.length - count, buf.length);
	}
	
	public synchronized void writeTo(OutputStream out) throws IOException
	{
		out.write(buf, buf.length - count, count);
	}
	
	public synchronized void reset()
	{
		count = 0;
	}
	
	/* public static void main(String[] args) throws IOException {
	 * VTInvertedByteArrayOutputStream stream = new
	 * VTInvertedByteArrayOutputStream(); byte[] data =
	 * "1234567890123456789012345678901234567890123456789012345678901234567890".
	 * getBytes(); stream.write(data); System.out.println("[" + new String(data)
	 * + "]"); System.out.println("[" + new String(stream.toByteArray()) + "]");
	 * } */
}