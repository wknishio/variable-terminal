package org.vate.stream.endian;

import java.io.IOException;
import java.io.OutputStream;

public class VTLittleEndianOutputStream extends OutputStream
{
	private byte[] shortBuffer;
	private byte[] ushortBuffer;
	private byte[] charBuffer;
	private byte[] subintBuffer;
	private byte[] intBuffer;
	private byte[] longBuffer;
	private OutputStream out;
	
	public VTLittleEndianOutputStream(OutputStream out)
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
		out.close();
	}
	
	public void writeBoolean(boolean b) throws IOException
	{
		out.write(b ? 1 : 0);
	}
	
	public void writeShort(short s) throws IOException
	{
		shortBuffer[0] = (byte) s;
		shortBuffer[1] = (byte) (s >> 8);
		out.write(shortBuffer);
	}
	
	public void writeUnsignedShort(int s) throws IOException
	{
		ushortBuffer[0] = (byte) s;
		ushortBuffer[1] = (byte) (s >> 8);
		out.write(ushortBuffer);
	}
	
	public void writeChar(char c) throws IOException
	{
		charBuffer[0] = (byte) c;
		charBuffer[1] = (byte) (c >> 8);
		out.write(charBuffer);
	}
	
	public void writeSubInt(int i) throws IOException
	{
		subintBuffer[0] = (byte) (i);
		subintBuffer[1] = (byte) (i >> 8);
		subintBuffer[2] = (byte) (i >> 16);
		out.write(subintBuffer);
	}
	
	public void writeInt(int i) throws IOException
	{
		intBuffer[0] = (byte) i;
		intBuffer[1] = (byte) (i >> 8);
		intBuffer[2] = (byte) (i >> 16);
		intBuffer[3] = (byte) (i >> 24);
		out.write(intBuffer);
	}
	
	public void writeUnsignedInt(long l) throws IOException
	{
		intBuffer[0] = (byte) l;
		intBuffer[1] = (byte) (l >> 8);
		intBuffer[2] = (byte) (l >> 16);
		intBuffer[3] = (byte) (l >> 24);
		out.write(intBuffer);
	}
	
	public void writeLong(long l) throws IOException
	{
		longBuffer[0] = (byte) l;
		longBuffer[1] = (byte) (l >> 8);
		longBuffer[2] = (byte) (l >> 16);
		longBuffer[3] = (byte) (l >> 24);
		longBuffer[4] = (byte) (l >> 32);
		longBuffer[5] = (byte) (l >> 40);
		longBuffer[6] = (byte) (l >> 48);
		longBuffer[7] = (byte) (l >> 56);
		out.write(longBuffer);
	}
	
	public void writeFloat(float f) throws Exception
	{
		writeInt(Float.floatToRawIntBits(f));
	}
	
	public void writeDouble(double d) throws IOException
	{
		writeLong(Double.doubleToRawLongBits(d));
	}
}