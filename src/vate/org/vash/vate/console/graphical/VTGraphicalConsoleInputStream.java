package org.vash.vate.console.graphical;

import java.io.IOException;
import java.io.InputStream;

public class VTGraphicalConsoleInputStream extends InputStream
{
  private volatile byte[] lineBuffer;
  private volatile int readed;
  
  public VTGraphicalConsoleInputStream()
  {
    lineBuffer = new byte[0];
  }
  
  public int read() throws IOException
  {
    if (lineBuffer == null || readed >= lineBuffer.length)
    {
      try
      {
        lineBuffer = (VTGraphicalConsoleReader.readLine(true) + "\n").getBytes("UTF-8");
        readed = 0;
      }
      catch (InterruptedException e)
      {
        return -1;
      }
    }
    return lineBuffer[readed++];
  }
  
  public int available() throws IOException
  {
    return lineBuffer.length - readed;
  }
  
  /*
   * public void close() throws IOException { VTGraphicalConsoleReader.close();
   * }
   */
  
  /*
   * public synchronized void mark(int readlimit) { super.mark(readlimit); }
   */
  
  /* public boolean markSupported() { return super.markSupported(); } */
  
  public int read(byte[] b, int off, int len) throws IOException
  {
    int transferred;
    for (transferred = 0; transferred < len; transferred++)
    {
      if (lineBuffer == null || readed >= lineBuffer.length)
      {
        if (transferred == 0)
        {
          try
          {
            lineBuffer = (VTGraphicalConsoleReader.readLine(true) + "\n").getBytes("UTF-8");
            readed = 0;
          }
          catch (InterruptedException e)
          {
            return transferred;
          }
        }
        else
        {
          return transferred;
        }
      }
      b[off + transferred] = lineBuffer[readed++];
    }
    return transferred;
  }
  
  public int read(byte[] b) throws IOException
  {
    return super.read(b);
  }
  
  /* public synchronized void reset() throws IOException { super.reset(); } */
  
  public long skip(long n) throws IOException
  {
    return super.skip(n);
  }
}