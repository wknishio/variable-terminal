package org.vash.vate.stream.data;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public interface VTRandomAccessDataOutput extends DataOutput
{
  public void write(byte[] b) throws IOException;
  
  public void write(byte[] b, int off, int len) throws IOException;
  
  public void flush() throws IOException;
  
  public void close() throws IOException;
  
  public void writeShort(short s) throws IOException;
  
  public void writeUnsignedShort(int s) throws IOException;
  
  public void writeChar(char c) throws IOException;
  
  public void writeSubInt(int i) throws IOException;
  
  public int getOutputCount();
  
  public void setOutputCount(int count);
  
  public void setOutputSize(int size);
  
  public byte[] getBuffer();
  
  public void setBuffer(byte[] buffer);
  
  public void setBuffer(byte[] buffer, int count);
  
  public void putByte(int index, byte val) throws IOException;
  
  public void putShort(int index, short val) throws IOException;
  
  public void putInt(int index, int val) throws IOException;
  
  public void putSubInt(int index, int val) throws IOException;
  
  public void putFloat(int index, float val) throws IOException;
  
  public void putLong(int index, long val) throws IOException;
  
  public void putDouble(int index, double val) throws IOException;
  
  public void putBytes(int index, byte[] data, int off, int len) throws IOException;
  
  public void putByte(byte val) throws IOException;
  
  public void putShort(short val) throws IOException;
  
  public void putInt(int val) throws IOException;
  
  public void putSubInt(int val) throws IOException;
  
  public void putFloat(float val) throws IOException;
  
  public void putLong(long val) throws IOException;
  
  public void putDouble(double val) throws IOException;
  
  public void putBytes(byte[] data, int off, int len) throws IOException;
  
  public OutputStream getOutputStream();
}