package org.vash.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTAutoFlushOutputStream extends FilterOutputStream
{
  public VTAutoFlushOutputStream(OutputStream out)
  {
    super(out);
  }
  
  public void out(OutputStream out)
  {
    this.out = out;
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    out.write(b, off, len);
    out.flush();
  }
  
  public final void write(byte[] b) throws IOException
  {
    out.write(b);
    out.flush();
  }
  
  public final void write(int b) throws IOException
  {
    out.write(b);
    out.flush();
  }
  
  public final void flush() throws IOException
  {
    out.flush();
  }
  
  public final void close() throws IOException
  {
    out.flush();
    out.close();
  }
}
