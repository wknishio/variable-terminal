package org.vash.vate.stream.array;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTFlushBufferedOutputStream extends FilterOutputStream
{
  private final VTByteArrayOutputStream buf;
  private final OutputStream out;
  
  public VTFlushBufferedOutputStream(VTByteArrayOutputStream buf, OutputStream out)
  {
    super(buf);
    this.buf = buf;
    this.out = out;
  }
  
  public final void flush() throws IOException
  {
    if (buf.count() > 0)
    {
      // System.out.println("flushing:" + buf.count());
      buf.writeTo(out);
      buf.reset();
      out.flush();
    }
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
