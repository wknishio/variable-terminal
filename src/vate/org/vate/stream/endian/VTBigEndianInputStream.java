package org.vate.stream.endian;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class VTBigEndianInputStream extends InputStream
{
	// private int readed;
	// private int total;
	// private int remaining;
	private byte[] shortBuffer;
	private byte[] ushortBuffer;
	private byte[] charBuffer;
	private byte[] subintBuffer;
	private byte[] intBuffer;
	private byte[] longBuffer;
	private InputStream in;
	
	public VTBigEndianInputStream(InputStream in)
	{
		this.in = in;
		this.shortBuffer = new byte[2];
		this.ushortBuffer = new byte[2];
		this.charBuffer = new byte[2];
		this.subintBuffer = new byte[3];
		this.intBuffer = new byte[4];
		this.longBuffer = new byte[8];
	}
	
	public void setIntputStream(InputStream in)
	{
		this.in = in;
	}
	
	public int read() throws IOException
	{
		return in.read();
	}
	
	public int read(byte[] b) throws IOException
	{
		return in.read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
	
	public int available() throws IOException
	{
		return in.available();
	}
	
	public long skip(long l) throws IOException
	{
		return in.skip(l);
	}
	
	public void close() throws IOException
	{
		in.close();
	}
	
	public boolean readBoolean() throws IOException
	{
		return in.read() != 0;
	}
	
	public byte readByte() throws IOException
	{
		int b = in.read();
		if (b == -1)
		{
			throw new EOFException();
		}
		return (byte) b;
	}
	
	public short readShort() throws IOException
	{
		readFully(shortBuffer);
		return (short) ((shortBuffer[0] & 0xFF) << 8 | (shortBuffer[1] & 0xFF));
	}
	
	public int readUnsignedShort() throws IOException
	{
		readFully(ushortBuffer);
		return ((ushortBuffer[0] & 0xFF) << 8 | (ushortBuffer[1] & 0xFF));
	}
	
	public char readChar() throws IOException
	{
		readFully(charBuffer);
		return (char) ((charBuffer[0] & 0xFF) << 8 | (charBuffer[1] & 0xFF));
	}
	
	public int readSubInt() throws IOException
	{
		readFully(subintBuffer);
		return ((subintBuffer[0] & 0x80) << 24 | (subintBuffer[0] & 0xFF) << 16 | (subintBuffer[1] & 0xFF) << 8 | (subintBuffer[2] & 0xFF));
	}
	
	public int readInt() throws IOException
	{
		readFully(intBuffer);
		return ((intBuffer[0] & 0xFF) << 24 | (intBuffer[1] & 0xFF) << 16 | (intBuffer[2] & 0xFF) << 8 | (intBuffer[3] & 0xFF));
	}
	
	public long readLong() throws IOException
	{
		readFully(longBuffer);
		return (((long) (longBuffer[0] & 0xFF)) << 56 | ((long) (longBuffer[1] & 0xFF)) << 48 | ((long) (longBuffer[2] & 0xFF)) << 40 | ((long) (longBuffer[3] & 0xFF)) << 32 | ((long) (longBuffer[4] & 0xFF)) << 24 | ((long) (longBuffer[5] & 0xFF)) << 16 | ((long) (longBuffer[6] & 0xFF)) << 8 | ((long) (longBuffer[7] & 0xFF)));
	}
	
	public float readFloat() throws Exception
	{
		return Float.intBitsToFloat(readInt());
	}
	
	public double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}
	
	public final void readFully(byte b[]) throws IOException
	{
		readFully(b, 0, b.length);
	}
	
	public final void readFully(byte b[], int off, int len) throws IOException
	{
		if (len < 0)
		{
			throw new IndexOutOfBoundsException();
		}
		int total = 0;
		int readed = 0;
		while (total < len)
		{
			readed = in.read(b, off + total, len - total);
			if (readed < 0)
			{
				throw new EOFException();
			}
			total += readed;
		}
	}
}
