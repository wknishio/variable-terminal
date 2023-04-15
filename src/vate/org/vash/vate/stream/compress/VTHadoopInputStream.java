package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;

import io.airlift.compress.hadoop.HadoopInputStream;

public class VTHadoopInputStream extends InputStream
{
  private HadoopInputStream in;
  
  public VTHadoopInputStream(HadoopInputStream in)
  {
    this.in = in;
  }
  
  public int available() throws IOException
  {
    return in.available();
  }
  
  public int read(byte[] data, int off, int len) throws IOException
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
