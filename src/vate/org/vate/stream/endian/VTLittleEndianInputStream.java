package org.vate.stream.endian;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class VTLittleEndianInputStream extends InputStream
{
	private byte[] shortBuffer;
	private byte[] ushortBuffer;
	private byte[] charBuffer;
	private byte[] subintBuffer;
	private byte[] intBuffer;
	private byte[] longBuffer;
	private InputStream in;
	
	public VTLittleEndianInputStream(InputStream in)
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
	
	public int readByte() throws IOException
	{
		int b = in.read();
		if (b == -1)
		{
			throw new EOFException();
		}
		return b;
	}
	
	public short readShort() throws IOException
	{
		readFully(shortBuffer);
		return (short) ((shortBuffer[0] & 0xFF) | (shortBuffer[1] & 0xFF) << 8);
	}
	
	public int readUnsignedShort() throws IOException
	{
		readFully(ushortBuffer);
		return ((ushortBuffer[0] & 0xFF) | (ushortBuffer[1] & 0xFF) << 8);
	}
	
	public char readChar() throws IOException
	{
		readFully(charBuffer);
		return (char) ((charBuffer[0] & 0xFF) | (charBuffer[1] & 0xFF) << 8);
	}
	
	public int readSubInt() throws IOException
	{
		readFully(subintBuffer);
		return ((subintBuffer[0] & 0xFF) | (subintBuffer[1] & 0xFF) << 8 | (subintBuffer[2] & 0xFF) << 16);
	}
	
	public int readInt() throws IOException
	{
		readFully(intBuffer);
		return ((intBuffer[0] & 0xFF) | (intBuffer[1] & 0xFF) << 8 | (intBuffer[2] & 0xFF) << 16 | (intBuffer[3] & 0xFF) << 24);
	}
	
	public long readUnsignedInt() throws IOException
	{
		readFully(intBuffer);
		return ((intBuffer[0] & 0xFF) | (intBuffer[1] & 0xFF) << 8 | (intBuffer[2] & 0xFF) << 16 | (intBuffer[3] & 0xFF) << 24);
	}
	
	public long readLong() throws IOException
	{
		readFully(longBuffer);
		return (((long) (longBuffer[0] & 0xFF)) | ((long) (longBuffer[1] & 0xFF)) << 8 | ((long) (longBuffer[2] & 0xFF)) << 16 | ((long) (longBuffer[3] & 0xFF)) << 24 | ((long) (longBuffer[4] & 0xFF)) << 32 | ((long) (longBuffer[5] & 0xFF)) << 40 | ((long) (longBuffer[6] & 0xFF)) << 48 | ((long) (longBuffer[7] & 0xFF)) << 56);
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