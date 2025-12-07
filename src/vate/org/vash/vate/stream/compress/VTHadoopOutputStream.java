package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;

import org.vash.vate.io.airlift.compress.hadoop.HadoopOutputStream;

public class VTHadoopOutputStream extends OutputStream
{
  private final HadoopOutputStream out;
  
  public VTHadoopOutputStream(final HadoopOutputStream out)
  {
    this.out = out;
  }
  
  public void write(final byte[] b, final int off, final int len) throws IOException
  {
    out.write(b, off, len);
  }
  
  public void write(final byte[] b) throws IOException
  {
    out.write(b);
  }
  
  public void write(final int b) throws IOException
  {
    out.write(b);
  }
  
  public void flush() throws IOException
  {
    out.flush();
    //out.finish();
    //out.flush();
  }
  
  public void close() throws IOException
  {
    out.close();
  }
}