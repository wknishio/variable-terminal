package org.vash.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBlockSplitOutputStream extends FilterOutputStream
{
  private final int blockSize;
  
  public VTBlockSplitOutputStream(OutputStream out, int blockSize)
  {
    super(out);
    this.blockSize = blockSize;
  }
  
  public final void write(byte[] b) throws IOException
  {
    int off = 0;
    int len = b.length;
    if (len <= blockSize)
    {
      out.write(b, off, len);
    }
    else
    {
      int amount;
      while (len > 0)
      {
        amount = Math.min(blockSize, len);
        out.write(b, off, amount);
        off += amount;
        len -= amount;
      }
    }
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    if (len <= blockSize)
    {
      out.write(b, off, len);
    }
    else
    {
      int amount;
      while (len > 0)
      {
        amount = Math.min(blockSize, len);
        out.write(b, off, amount);
        off += amount;
        len -= amount;
      }
    }
  }
  
  public final void write(int b) throws IOException
  {
    out.write(b);
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
}