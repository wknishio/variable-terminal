package org.vash.vate.stream.endian;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBigEndianOutputStream extends OutputStream implements DataOutput
{
  private final byte[] shortBuffer;
  private final byte[] ushortBuffer;
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
  
  public final void write(final int b) throws IOException
  {
    out.write(b);
  }
  
  public final void write(final byte[] b) throws IOException
  {
    out.write(b);
  }
  
  public final void write(final byte[] b, final int off, final int len) throws IOException
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
  
  public final void writeBoolean(final boolean b) throws IOException
  {
    out.write(b ? 1 : 0);
  }
  
  public final void writeShort(final short s) throws IOException
  {
    shortBuffer[0] = (byte) (s >> 8);
    shortBuffer[1] = (byte) s;
    out.write(shortBuffer);
  }
  
  public final void writeUnsignedShort(final int s) throws IOException
  {
    ushortBuffer[0] = (byte) (s >> 8);
    ushortBuffer[1] = (byte) s;
    out.write(ushortBuffer);
  }
  
  public final void writeChar(final char c) throws IOException
  {
    charBuffer[0] = (byte) (c >> 8);
    charBuffer[1] = (byte) c;
    out.write(charBuffer);
  }
  
  public final void writeSubInt(final int i) throws IOException
  {
    subintBuffer[0] = (byte) (i >> 16);
    subintBuffer[1] = (byte) (i >> 8);
    subintBuffer[2] = (byte) (i);
    out.write(subintBuffer);
  }
  
  public final void writeInt(final int i) throws IOException
  {
    intBuffer[0] = (byte) (i >> 24);
    intBuffer[1] = (byte) (i >> 16);
    intBuffer[2] = (byte) (i >> 8);
    intBuffer[3] = (byte) i;
    out.write(intBuffer);
  }
  
  public final void writeLong(final long l) throws IOException
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
  
  public final void writeFloat(final float f) throws IOException
  {
    writeInt(Float.floatToRawIntBits(f));
  }
  
  public final void writeDouble(final double d) throws IOException
  {
    writeLong(Double.doubleToRawLongBits(d));
  }
  
  public final void writeByte(final int v) throws IOException
  {
    write(v);
  }
  
  public final void writeShort(final int v) throws IOException
  {
    writeShort((short) v);
  }
  
  public final void writeChar(final int v) throws IOException
  {
    writeUnsignedShort(v);
  }
  
  public final void writeData(final byte[] b, final int off, final int len) throws IOException
  {
    byte[] data = new byte[4 + len];
    data[0] = (byte) (len >> 24);
    data[1] = (byte) (len >> 16);
    data[2] = (byte) (len >> 8);
    data[3] = (byte) len;
    System.arraycopy(b, off, data, 4, len);
    write(data, 0, data.length);
  }
  
  public final void writeData(final byte[] b) throws IOException
  {
    writeData(b, 0, b.length);
  }
  
  public final void writeUTF8(final String s) throws IOException
  {
    byte[] utf = s.getBytes("UTF-8");
    writeData(utf);
  }
  
  public final void writeUTF(final String s) throws IOException
  {
    writeUTF8(s);
  }
  
  public final void writeLine(final String s) throws IOException
  {
    writeUTF(s);
  }
  
  public final void write(final String s) throws IOException
  {
    writeUTF(s);
  }
  
  public final void write(char[] buf, final int off, final int len) throws IOException
  {
    byte[] utf = String.valueOf(buf, off, len).getBytes("UTF-8");
    writeData(utf);
  }
  
  public final void write(char[] buf) throws IOException
  {
    write(buf, 0, buf.length);
  }
  
  public final void writeBytes(final String s) throws IOException
  {
    char[] chars = s.toCharArray();
    byte[] bytes = new byte[chars.length];
    for (int i = 0; i < bytes.length; i++)
    {
      bytes[i] = (byte) chars[i];
    }
    write(bytes);
  }
  
  public final void writeChars(final String s) throws IOException
  {
    char[] chars = s.toCharArray();
    byte[] bytes = new byte[chars.length * 2];
    for (int i = 0; i < bytes.length; i += 2)
    {
      bytes[i] = (byte) (chars[i / 2] >> 8);
      bytes[i + 1] = (byte) (chars[i / 2]);
    }
    write(bytes);
  }
}