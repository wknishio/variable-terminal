package org.vate.stream.endian;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBigEndianOutputStream extends OutputStream implements DataOutput
{
	private byte[] ushortBuffer;
	private byte[] shortBuffer;
	private byte[] charBuffer;
	private byte[] subintBuffer;
	private byte[] intBuffer;
	private byte[] longBuffer;
	private OutputStream out;
	
	public VTBigEndianOutputStream(OutputStream out)
	{
		this.out = out;
		this.shortBuffer = new byte[2];
		this.ushortBuffer = new byte[2];
		this.charBuffer = new byte[2];
		this.subintBuffer = new byte[3];
		this.intBuffer = new byte[4];
		this.longBuffer = new byte[8];
	}
	
	public void setOutputStream(OutputStream out)
	{
		this.out = out;
	}
	
	public void write(int b) throws IOException
	{
		out.write(b);
	}
	
	public void write(byte[] b) throws IOException
	{
		out.write(b);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
	}
	
	public void flush() throws IOException
	{
		out.flush();
	}
	
	public void close() throws IOException
	{
//		try
//		{
//			flush();
//		}
//		catch (Throwable t)
//		{
//			
//		}
		out.close();
	}
	
	public void writeBoolean(boolean b) throws IOException
	{
		out.write(b ? 1 : 0);
	}
	
	public void writeShort(short s) throws IOException
	{
		shortBuffer[0] = (byte) (s >> 8);
		shortBuffer[1] = (byte) s;
		out.write(shortBuffer);
	}
	
	public void writeUnsignedShort(int s) throws IOException
	{
		ushortBuffer[0] = (byte) (s >> 8);
		ushortBuffer[1] = (byte) s;
		out.write(ushortBuffer);
	}
	
	public void writeChar(char c) throws IOException
	{
		charBuffer[0] = (byte) (c >> 8);
		charBuffer[1] = (byte) c;
		out.write(charBuffer);
	}
	
	public void writeSubInt(int i) throws IOException
	{
		subintBuffer[0] = (byte) (i >> 16);
		subintBuffer[1] = (byte) (i >> 8);
		subintBuffer[2] = (byte) (i);
		out.write(subintBuffer);
	}
	
	public void writeInt(int i) throws IOException
	{
		intBuffer[0] = (byte) (i >> 24);
		intBuffer[1] = (byte) (i >> 16);
		intBuffer[2] = (byte) (i >> 8);
		intBuffer[3] = (byte) i;
		out.write(intBuffer);
	}
	
	public void writeLong(long l) throws IOException
	{
		longBuffer[0] = (byte) (l >> 56);
		longBuffer[1] = (byte) (l >> 48);
		longBuffer[2] = (byte) (l >> 40);
		longBuffer[3] = (byte) (l >> 32);
		longBuffer[4] = (byte) (l >> 24);
		longBuffer[5] = (byte) (l >> 16);
		longBuffer[6] = (byte) (l >> 8);
		longBuffer[7] = (byte) l;
		out.write(longBuffer);
	}
	
	public void writeFloat(float f) throws IOException
	{
		writeInt(Float.floatToRawIntBits(f));
	}
	
	public void writeDouble(double d) throws IOException
	{
		writeLong(Double.doubleToRawLongBits(d));
	}

	public void writeByte(int v) throws IOException
	{
		write(v);
	}

	public void writeShort(int v) throws IOException
	{
		writeUnsignedShort(v);
	}

	public void writeChar(int v) throws IOException
	{
		writeUnsignedShort(v);
	}

	public void writeBytes(String s) throws IOException
	{
		throw new IOException("not implemented");
	}

	public void writeChars(String s) throws IOException
	{
		throw new IOException("not implemented");
	}

	public void writeUTF(String s) throws IOException
	{
		throw new IOException("not implemented");
	}
}