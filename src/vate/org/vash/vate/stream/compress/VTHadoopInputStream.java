package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;

import io.airlift.compress.hadoop.HadoopInputStream;

public class VTHadoopInputStream extends InputStream
{
  private final HadoopInputStream in;
  
  public VTHadoopInputStream(final HadoopInputStream in)
  {
    this.in = in;
  }
  
  public int available() throws IOException
  {
    return in.available();
  }
  
  public int read(final byte[] data, final int off, final int len) throws IOException
  {
    return in.read(data, off, len);
  }
  
  public int read() throws IOException
  {
    return in.read();
  }
    
  public void close() throws IOException
  {
    in.close();
  }
}
