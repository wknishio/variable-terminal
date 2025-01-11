package org.vash.vate.stream.array;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTFlushBufferedOutputStream extends FilterOutputStream
{
  private final OutputStream out;
  private final VTByteArrayOutputStream buf;
  
  public VTFlushBufferedOutputStream(OutputStream out, VTByteArrayOutputStream buf)
  {
    super(buf);
    this.out = out;
    this.buf = buf;
  }
  
  public void write(int b)
  {
    buf.write(b);
  }
  
  public void write(byte[] b, int off, int len)
  {
    buf.write(b, off, len);
  }
  
  public void flush() throws IOException
  {
    if (buf.count() > 0)
    {
      // System.out.println("flushing:" + buf.count());
      buf.writeTo(out);
      buf.reset();
      out.flush();
    }
  }
  
  public void close() throws IOException
  {
    out.close();
  }
}