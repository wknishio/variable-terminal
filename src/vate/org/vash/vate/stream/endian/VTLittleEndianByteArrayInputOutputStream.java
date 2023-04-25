package org.vash.vate.stream.endian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.data.RandomAccessDataInputOutput;

public final class VTLittleEndianByteArrayInputOutputStream implements RandomAccessDataInputOutput
{
  private final VTByteArrayOutputStream output;
  private final VTByteArrayInputStream input;
  private final VTLittleEndianInputStream dataInput;
  private final VTLittleEndianOutputStream dataOutput;
  
  public VTLittleEndianByteArrayInputOutputStream(int size)
  {
    output = new VTByteArrayOutputStream(size);
    input = new VTByteArrayInputStream(output.buf(), 0, size);
    dataOutput = new VTLittleEndianOutputStream(output);
    dataInput = new VTLittleEndianInputStream(input);
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
  
  public final void readFully(byte[] b) throws IOException
  {
    dataInput.readFully(b);
  }
  
  public final void readFully(byte[] b, int off, int len) throws IOException
  {
    dataInput.readFully(b, off, len);
  }
  
  public final int skipBytes(int n) throws IOException
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
  
  public final int read(byte[] b) throws IOException
  {
    return input.read(b);
  }
  
  public final int read(byte[] b, int off, int len) throws IOException
  {
    return input.read(b, off, len);
  }
  
  public final int available() throws IOException
  {
    return input.available();
  }
  
  public final long skip(long l) throws IOException
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
  
  public final void write(int b) throws IOException
  {
    output.write(b);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeBoolean(boolean v) throws IOException
  {
    dataOutput.writeBoolean(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeByte(int v) throws IOException
  {
    dataOutput.writeByte(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeShort(int v) throws IOException
  {
    dataOutput.writeShort(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChar(int v) throws IOException
  {
    dataOutput.writeChar(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeInt(int v) throws IOException
  {
    dataOutput.writeInt(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeLong(long v) throws IOException
  {
    dataOutput.writeLong(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeFloat(float v) throws IOException
  {
    dataOutput.writeFloat(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeDouble(double v) throws IOException
  {
    dataOutput.writeDouble(v);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeBytes(String s) throws IOException
  {
    dataOutput.writeBytes(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChars(String s) throws IOException
  {
    dataOutput.writeChars(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeUTF(String s) throws IOException
  {
    dataOutput.writeUTF(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void write(byte[] b) throws IOException
  {
    output.write(b);
    input.buf(output.buf(), output.count());
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    output.write(b, off, len);
    input.buf(output.buf(), output.count());
  }
  
  public final void flush() throws IOException
  {
    output.flush();
    input.buf(output.buf(), output.count());
  }
  
  public final void writeShort(short s) throws IOException
  {
    dataOutput.writeShort(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeUnsignedShort(int s) throws IOException
  {
    dataOutput.writeUnsignedShort(s);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeChar(char c) throws IOException
  {
    dataOutput.writeChar(c);
    input.buf(output.buf(), output.count());
  }
  
  public final void writeSubInt(int i) throws IOException
  {
    dataOutput.writeSubInt(i);
    input.buf(output.buf(), output.count());
  }
  
  public final int getInputPos()
  {
    return input.pos();
  }
  
  public final void setInputPos(int pos)
  {
    input.pos(pos);
  }
  
  public final int getOutputCount()
  {
    return output.count();
  }
  
  public final void setOutputCount(int count)
  {
    output.count(count);
    input.count(output.count());
  }
  
  public final byte[] getBuffer()
  {
    return output.buf();
  }
  
  public final void setBuffer(byte[] buffer)
  {
    output.buf(buffer);
    input.buf(buffer);
  }
  
  public final void setBuffer(byte[] buffer, int count)
  {
    output.buf(buffer, count);
    input.buf(buffer, count);
  }
  
  
  public final void setOutputSize(int size)
  {
    if (output.buf().length < size)
    {
      byte[] nextbuf = new byte[size];
      System.arraycopy(output.buf(), 0, nextbuf, 0, output.buf().length);
      output.buf(nextbuf);
      input.buf(nextbuf);
    }
  }
  
  public final byte getByte(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readByte();
  }
  
  public final short getShort(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readShort();
  }
  
  public final int getInt(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readInt();
  }
  
  public final int getSubInt(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readSubInt();
  }
  
  public final float getFloat(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readFloat();
  }
  
  public final long getLong(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readLong();
  }
  
  public final double getDouble(int index) throws IOException
  {
    setInputPos(index);
    return dataInput.readDouble();
  }
  
  public final int getBytes(int index, byte[] data, int off, int len) throws IOException
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
  
  public final int getBytes(byte[] data, int off, int len) throws IOException
  {
    return input.read(data, off, len);
  }
  
  public final void putByte(int index, byte val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeByte(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putShort(int index, short val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeShort(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putInt(int index, int val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putSubInt(int index, int val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeSubInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putFloat(int index, float val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeFloat(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putLong(int index, long val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeLong(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putDouble(int index, double val) throws IOException
  {
    setOutputCount(index);
    dataOutput.writeDouble(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putBytes(int index, byte[] data, int off, int len) throws IOException
  {
    setOutputCount(index);
    output.write(data, off, len);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putByte(byte val) throws IOException
  {
    dataOutput.writeByte(val);
    input.buf(output.buf());
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putShort(short val) throws IOException
  {
    dataOutput.writeShort(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putInt(int val) throws IOException
  {
    dataOutput.writeInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putSubInt(int val) throws IOException
  {
    dataOutput.writeSubInt(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putFloat(float val) throws IOException
  {
    dataOutput.writeFloat(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putLong(long val) throws IOException
  {
    dataOutput.writeLong(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putDouble(double val) throws IOException
  {
    dataOutput.writeDouble(val);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
  
  public final void putBytes(byte[] data, int off, int len) throws IOException
  {
    output.write(data, off, len);
    input.buf(output.buf(), output.count());
    // return output.count();
  }
}