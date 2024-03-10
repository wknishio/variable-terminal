package org.vash.vate.stream.endian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.data.RandomAccessDataInputOutput;

public final class VTBigEndianByteArrayInputOutputStream implements RandomAccessDataInputOutput
{
  private final VTByteArrayOutputStream output;
  private final VTByteArrayInputStream input;
  private final VTBigEndianInputStream dataInput;
  private final VTBigEndianOutputStream dataOutput;
  
  public VTBigEndianByteArrayInputOutputStream(final int size)
  {
    output = new VTByteArrayOutputStream(size);
    input = new VTByteArrayInputStream(output.buf(), 0, size);
    dataOutput = new VTBigEndianOutputStream(output);
    dataInput = new VTBigEndianInputStream(input);
  }
  
  public InputStream getInputStream()
  {
    return dataInput;
  }
  
  public OutputStream getOutputStream()
  {
    return dataOutput;
  }
  
  public void reset() throws IOException
  {
    input.reset();
    output.reset();
  }
  
  public final void readFully(final byte[] b) throws IOException
  {
    dataInput.readFully(b);
  }
  
  public final void readFully(final byte[] b, final int off, final int len) throws IOException
  {
    dataInput.readFully(b, off, len);
  }
  
  public final int skipBytes(final int n) throws IOException
  {
    return dataInput.skipBytes(n);
  }
  
  public final boolean readBoolean() throws IOException
  {
    return dataInput.readBoolean();
  }
  
  public final byte readByte() throws IOException
  {
    return dataInput.readByte();
  }
  
  public final int readUnsignedByte() throws IOException
  {
    return dataInput.readUnsignedByte();
  }
  
  public final short readShort() throws IOException
  {
    return dataInput.readShort();
  }
  
  public final int readUnsignedShort() throws IOException
  {
    return dataInput.readUnsignedShort();
  }
  
  public final char readChar() throws IOException
  {
    return dataInput.readChar();
  }
  
  public final int readInt() throws IOException
  {
    return dataInput.readInt();
  }
  
  public final long readLong() throws IOException
  {
    return dataInput.readLong();
  }
  
  public final float readFloat() throws IOException
  {
    return dataInput.readFloat();
  }
  
  public final double readDouble() throws IOException
  {
    return dataInput.readDouble();
  }
  
  public final String readLine() throws IOException
  {
    return dataInput.readLine();
  }
  
  public final String readUTF() throws IOException
  {
    return dataInput.readUTF();
  }
  
  public final int read(final byte[] b) throws IOException
  {
    return input.read(b);
  }
  
  public final int read(final byte[] b, final int off, final int len) throws IOException
  {
    return input.read(b, off, len);
  }
  
  public final int available() throws IOException
  {
    return input.available();
  }
  
  public final long skip(final long l) throws IOException
  {
    return input.skip(l);
  }
  
  public final void close() throws IOException
  {
    
  }
  
  public final int readSubInt() throws IOException
  {
    return dataInput.readSubInt();
  }
  
  public final long readUnsignedInt() throws IOException
  {
    return dataInput.readUnsignedInt();
  }
  
  public final void write(final int b) throws IOException
  {
    output.write(b);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeBoolean(final boolean v) throws IOException
  {
    dataOutput.writeBoolean(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeByte(final int v) throws IOException
  {
    dataOutput.writeByte(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeShort(final int v) throws IOException
  {
    dataOutput.writeShort(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChar(final int v) throws IOException
  {
    dataOutput.writeChar(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeInt(final int v) throws IOException
  {
    dataOutput.writeInt(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeLong(final long v) throws IOException
  {
    dataOutput.writeLong(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeFloat(final float v) throws IOException
  {
    dataOutput.writeFloat(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeDouble(final double v) throws IOException
  {
    dataOutput.writeDouble(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeBytes(final String s) throws IOException
  {
    dataOutput.writeBytes(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChars(final String s) throws IOException
  {
    dataOutput.writeChars(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeUTF(final String s) throws IOException
  {
    dataOutput.writeUTF(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void write(final byte[] b) throws IOException
  {
    output.write(b);
    input.buf(output.buf(), output.count());
  }
  
  public final void write(final byte[] b, final int off, final int len) throws IOException
  {
    output.write(b, off, len);
    input.buf(output.buf(), output.count());
  }
  
  public final void flush() throws IOException
  {
    output.flush();
    input.buf(output.buf(), output.count());
  }
  
  public final void writeShort(final short s) throws IOException
  {
    dataOutput.writeShort(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeUnsignedShort(final int s) throws IOException
  {
    dataOutput.writeUnsignedShort(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChar(final char c) throws IOException
  {
    dataOutput.writeChar(c);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeSubInt(final int i) throws IOException
  {
    dataOutput.writeSubInt(i);
    input.buf(output.buf(), output.count());
  }
  
  public final int getInputPos()
  {
    return input.pos();
  }
  
  public final void setInputPos(final int pos)
  {
    input.pos(pos);
  }
  
  public final int getOutputCount()
  {
    return output.count();
  }
  
  public final void setOutputCount(final int count)
  {
    output.count(count);
    input.count(output.count());
  }
  
  public final byte[] getBuffer()
  {
    return output.buf();
  }
  
  public final void setBuffer(final byte[] buffer)
  {
    output.buf(buffer);
    input.buf(buffer);
  }
  
  public final void setBuffer(final byte[] buffer, final int count)
  {
    output.buf(buffer, count);
    input.buf(buffer, count);
  }
  
  public final void setOutputSize(final int size)
  {
    if (output.buf().length < size)
    {
      byte[] nextbuf = new byte[size];
      System.arraycopy(output.buf(), 0, nextbuf, 0, output.buf().length);
      output.buf(nextbuf);
      input.buf(nextbuf);
    }
  }
  
  public final byte getByte(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readByte();
  }
  
  public final short getShort(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readShort();
  }
  
  public final int getInt(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readInt();
  }
  
  public final int getSubInt(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readSubInt();
  }
  
  public final float getFloat(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readFloat();
  }
  
  public final long getLong(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readLong();
  }
  
  public final double getDouble(final int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readDouble();
  }
  
  public final int getBytes(final int index, final byte[] data, final int off, final int len) throws IOException
  {
    setInputPos(index);
    return input.read(data, off, len);
  }
  
  public final byte getByte() throws IOException
  {
    return dataInput.readByte();
  }
  
  public final short getShort() throws IOException
  {
    return dataInput.readShort();
  }
  
  public final int getInt() throws IOException
  {
    return dataInput.readInt();
  }
  
  public final int getSubInt() throws IOException
  {
    return dataInput.readSubInt();
  }
  
  public final float getFloat() throws IOException
  {
    return dataInput.readFloat();
  }
  
  public final long getLong() throws IOException
  {
    return dataInput.readLong();
  }
  
  public final double getDouble() throws IOException
  {
    return dataInput.readDouble();
  }
  
  public final int getBytes(final byte[] data, final int off, final int len) throws IOException
  {
    return input.read(data, off, len);
  }
  
  public final void putByte(final int index, final byte val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeByte(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putShort(final int index, final short val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeShort(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putInt(final int index, final int val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putSubInt(final int index, final int val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeSubInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putFloat(final int index, final float val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeFloat(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putLong(final int index, final long val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeLong(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putDouble(final int index, final double val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeDouble(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putBytes(final int index, final byte[] data, final int off, final int len) throws IOException
  {
    setOutputCount(index);
    output.write(data, off, len);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putByte(final byte val) throws IOException
  {
    dataOutput.writeByte(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putShort(final short val) throws IOException
  {
    dataOutput.writeShort(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putInt(final int val) throws IOException
  {
    dataOutput.writeInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putSubInt(final int val) throws IOException
  {
    dataOutput.writeSubInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putFloat(final float val) throws IOException
  {
    dataOutput.writeFloat(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putLong(final long val) throws IOException
  {
    dataOutput.writeLong(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putDouble(final double val) throws IOException
  {
    dataOutput.writeDouble(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putBytes(final byte[] data, final int off, final int len) throws IOException
  {
    output.write(data, off, len);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
}