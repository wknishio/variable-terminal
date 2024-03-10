package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public final class VTSyncFlushDeflaterOutputStream extends DeflaterOutputStream
{
  private static final int SYNC_FLUSH = 2;
  private static Method deflateMethod;
  
  static
  {
    try
    {
      deflateMethod = DeflaterOutputStream.class.getMethod("deflate", new Class[]
      { byte[].class, Integer.class, Integer.class, Integer.class });
      // deflateMethod.setAccessible(true);
    }
    catch (Throwable t)
    {
      
    }
  }
  // public VTSyncFlushDeflaterOutputStream(OutputStream out, Deflater def, int
  // size)
  // {
  // super(out, def, size);
  // }
  
  public VTSyncFlushDeflaterOutputStream(final OutputStream out, final Deflater def, final int size) throws NoSuchMethodException
  {
    super(out, def, size);
    if (deflateMethod == null)
    {
      throw new NoSuchMethodException("Incompatible java version < 1.7");
    }
  }
  
  protected final void deflate() throws IOException
  {
    int len = 0;
    try
    {
      // int len = def.deflate(buf, 0, buf.length, SYNC_FLUSH);
      len = (Integer) deflateMethod.invoke(def, buf, 0, buf.length, SYNC_FLUSH);
    }
    catch (Throwable e)
    {
      throw new IOException(e.getMessage());
    }
    if (len > 0)
    {
      out.write(buf, 0, len);
    }
  }
  
  public final void flush() throws IOException
  {
    if (!def.finished())
    {
      int len = 0;
      try
      {
        // while ((len = def.deflate(buf, 0, buf.length, SYNC_FLUSH)) > 0)
        while ((len = (Integer) deflateMethod.invoke(def, buf, 0, buf.length, SYNC_FLUSH)) > 0)
        {
          out.write(buf, 0, len);
          if (len < buf.length)
          {
            break;
          }
        }
      }
      catch (Throwable e)
      {
        throw new IOException(e.getMessage());
      }
    }
    out.flush();
  }
}