package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class VTNoFlushDeflaterOutputStream extends DeflaterOutputStream
{
  public VTNoFlushDeflaterOutputStream(OutputStream out, Deflater deflater, int size)
  {
    super(out, deflater, size);
  }
  
  protected void deflate() throws IOException
  {
    int len = def.deflate(buf, 0, buf.length);
    if (len > 0)
    {
      //lout.writeInt(len);
      out.write(buf, 0, len);
    }
  }
  
  public void flush() throws IOException
  {
    finish();
    def.reset();
    out.flush();
  }
  
  public void close() throws IOException
  {
    flush();
    out.close();
  }
}