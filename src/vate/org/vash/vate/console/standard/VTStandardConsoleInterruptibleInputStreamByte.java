package org.vash.vate.console.standard;

import java.io.IOException;

public class VTStandardConsoleInterruptibleInputStreamByte extends VTStandardConsoleInterruptibleInputStream
{
  private byte[] inputBuffer;
  private int readed;
  private VTStandardConsoleInterruptibleReaderByte reader;
  private Thread currentThread;
  
  public VTStandardConsoleInterruptibleInputStreamByte(VTStandardConsole console)
  {
    super(console);
    this.reader = new VTStandardConsoleInterruptibleReaderByte();
  }
  
  public void setEcho(boolean echo)
  {
    reader.setEcho(echo);
  }
  
  public boolean usingRead()
  {
    return currentThread == null;
  }
  
  public void interruptRead()
  {
    if (currentThread != null)
    {
      try
      {
        currentThread.interrupt();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  private int readInputBuffer() throws IOException
  {
    currentThread = Thread.currentThread();
    int available = -1;
    try
    {
      inputBuffer = reader.read();
      readed = 0;
      available = inputBuffer.length;
    }
    catch (InterruptedException e)
    {
      
    }
    finally
    {
      currentThread = null;
    }
    return available;
  }
  
  public int read() throws IOException
  {
    if (inputBuffer == null || readed >= inputBuffer.length)
    {
      int lineSize = readInputBuffer();
      if (lineSize <= 0)
      {
        return lineSize;
      }
    }
    return inputBuffer[readed++];
  }
  
  public int read(byte[] b, int off, int len) throws IOException
  {
    int usable = 0;
    if (inputBuffer == null || readed >= inputBuffer.length)
    {
      int lineSize = readInputBuffer();
      if (lineSize <= 0)
      {
        return lineSize;
      }
    }
    usable = Math.min(inputBuffer.length - readed, len);
    System.arraycopy(inputBuffer, readed, b, off, usable);
    readed += usable;
    return usable;
  }
  
  public int read(byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }
  
  public int available() throws IOException
  {
    return inputBuffer.length - readed;
  }
  
  public void close() throws IOException
  {
    interruptRead();
    // unclosable!
  }
}