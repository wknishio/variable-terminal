package org.vash.vate.stream.limit;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class VTSizedInputStream extends FilterInputStream
{
  private int remaining = 0;
  // private long skipped = 0;
  
  public VTSizedInputStream(final InputStream in)
  {
    super(in);
  }
  
  public final void size(final int size)
  {
    this.remaining = size;
  }
  
  public final int available() throws IOException
  {
    int available = in.available();
    return Math.min(remaining, available);
  }
  
  public final void finish() throws IOException
  {
    long skipped = 0;
    while (remaining > 0 && skipped >= 0)
    {
      skipped = in.skip(remaining);
      if (skipped < 0)
      {
        remaining = -1;
        return;
      }
      remaining -= skipped;
    }
  }
  
  public final int read() throws IOException
  {
    if (remaining <= 0)
    {
      return -1;
    }
    int data = in.read();
    if (data < 0)
    {
      remaining = -1;
      return data;
    }
    remaining--;
    return data;
  }
  
  public final int read(final byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }
  
  public final int read(final byte[] b, final int off, final int len) throws IOException
  {
    if (remaining <= 0)
    {
      return -1;
    }
    else
    {
      int readed = in.read(b, off, Math.min(remaining, len));
      if (readed < 0)
      {
        remaining = -1;
        return readed;
      }
      remaining -= readed;
      return readed;
    }
  }
  
  public final void close() throws IOException
  {
    
  }
  
  public final void forceClose() throws IOException
  {
    in.close();
  }
}