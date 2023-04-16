package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;

import io.airlift.compress.hadoop.HadoopOutputStream;

public class VTHadoopOutputStream extends OutputStream
{
  private HadoopOutputStream out;
  
  public VTHadoopOutputStream(HadoopOutputStream out)
  {
    this.out = out;
  }
  
  public void write(byte[] b, int off, int len) throws IOException
  {
    out.write(b, off, len);
  }
  
  public void write(byte[] b) throws IOException
  {
    out.write(b);
  }
  
  public void write(int b) throws IOException
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
