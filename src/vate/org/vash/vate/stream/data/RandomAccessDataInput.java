package org.vash.vate.stream.data;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

public interface RandomAccessDataInput extends DataInput
{
  public int read(byte[] b) throws IOException;
  
  public int read(byte[] b, int off, int len) throws IOException;
  
  public int available() throws IOException;
  
  public long skip(long l) throws IOException;
  
  public void close() throws IOException;
  
  public int readSubInt() throws IOException;
  
  public long readUnsignedInt() throws IOException;
  
  public int getInputPos();
  
  public void setInputPos(int pos);
  
  public byte getByte(int index) throws IOException;
  
  public short getShort(int index) throws IOException;
  
  public int getInt(int index) throws IOException;
  
  public int getSubInt(int index) throws IOException;
  
  public float getFloat(int index) throws IOException;
  
  public long getLong(int index) throws IOException;
  
  public double getDouble(int index) throws IOException;
  
  public int getBytes(int index, byte[] data, int off, int len) throws IOException;
  
  public byte getByte() throws IOException;
  
  public short getShort() throws IOException;
  
  public int getInt() throws IOException;
  
  public int getSubInt() throws IOException;
  
  public float getFloat() throws IOException;
  
  public long getLong() throws IOException;
  
  public double getDouble() throws IOException;
  
  public int getBytes(byte[] data, int off, int len) throws IOException;
  
  public InputStream getInputStream();
}