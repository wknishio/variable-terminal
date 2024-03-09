package org.vash.vate.stream.endian;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBigEndianOutputStream extends OutputStream implements DataOutput
{
  private final byte[] ushortBuffer;
  private final byte[] shortBuffer;
  private final byte[] charBuffer;
  private final byte[] subintBuffer;
  private final byte[] intBuffer;
  private final byte[] longBuffer;
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
  
  public final void setOutputStream(OutputStream out)
  {
    this.out = out;
  }
  
  public final OutputStream getOutputStream()
  {
    return out;
  }
  
  public final void write(int b) throws IOException
  {
    out.write(b);
  }
  
  public final void write(byte[] b) throws IOException
  {
    out.write(b);
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    out.write(b, off, len);
  }
  
  public final void flush() throws IOException
  {
    out.flush();
  }
  
  public final void close() throws IOException
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
  
  public final void writeBoolean(boolean b) throws IOException
  {
    out.write(b ? 1 : 0);
  }
  
  public final void writeShort(short s) throws IOException
  {
    shortBuffer[0] = (byte) (s >> 8);
    shortBuffer[1] = (byte) s;
    out.write(shortBuffer);
  }
  
  public final void writeUnsignedShort(int s) throws IOException
  {
    ushortBuffer[0] = (byte) (s >> 8);
    ushortBuffer[1] = (byte) s;
    out.write(ushortBuffer);
  }
  
  public final void writeChar(char c) throws IOException
  {
    charBuffer[0] = (byte) (c >> 8);
    charBuffer[1] = (byte) c;
    out.write(charBuffer);
  }
  
  public final void writeSubInt(int i) throws IOException
  {
    subintBuffer[0] = (byte) (i >> 16);
    subintBuffer[1] = (byte) (i >> 8);
    subintBuffer[2] = (byte) (i);
    out.write(subintBuffer);
  }
  
  public final void writeInt(int i) throws IOException
  {
    intBuffer[0] = (byte) (i >> 24);
    intBuffer[1] = (byte) (i >> 16);
    intBuffer[2] = (byte) (i >> 8);
    intBuffer[3] = (byte) i;
    out.write(intBuffer);
  }
  
  public final void writeLong(long l) throws IOException
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
  
  public final void writeFloat(float f) throws IOException
  {
    writeInt(Float.floatToRawIntBits(f));
  }
  
  public final void writeDouble(double d) throws IOException
  {
    writeLong(Double.doubleToRawLongBits(d));
  }
  
  public final void writeByte(int v) throws IOException
  {
    write(v);
  }
  
  public final void writeShort(int v) throws IOException
  {
    writeShort((short) v);
  }
  
  public final void writeChar(int v) throws IOException
  {
    writeUnsignedShort(v);
  }
  
  public final void writeBytes(String s) throws IOException
  {
    writeUTF(s);
  }
  
  public final void writeChars(String s) throws IOException
  {
    writeUTF(s);
  }
  
  public final void writeData(byte[] b) throws IOException
  {
    int size = b.length;
    byte[] data = new byte[4 + size];
    data[0] = (byte) (size >> 24);
    data[1] = (byte) (size >> 16);
    data[2] = (byte) (size >> 8);
    data[3] = (byte) size;
    System.arraycopy(b, 0, data, 4, b.length);
    write(data, 0, data.length);
  }
  
  public final void writeUTF(String s) throws IOException
  {
    byte[] utf = s.getBytes("UTF-8");
    writeData(utf);
  }
  
  public final void writeLine(String s) throws IOException
  {
    byte[] utf = s.getBytes("UTF-8");
    writeData(utf);
  }
}