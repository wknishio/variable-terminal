package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public class VTNoFlushDeflaterOutputStream extends DeflaterOutputStream
{
  private VTLittleEndianOutputStream lout;
  //private int available = 0;
  
  public VTNoFlushDeflaterOutputStream(OutputStream out, Deflater deflater, int size)
  {
    super(out, deflater, size);
    this.lout = new VTLittleEndianOutputStream(out);
  }
  
  protected void deflate() throws IOException
  {
    int len = def.deflate(buf, 0, buf.length);
    if (len > 0)
    {
      //System.out.println("deflate.flush()");
      //lout.writeInt(available);
      lout.writeInt(len);
      out.write(buf, 0, len);
      //available = 0;
      //System.out.println("deflate.compressed(" + Arrays.toString(VTArrays.copyOfRange(buf, 0, len)) + ")");
    }
  }
  
  public void write(byte[] b, int off, int len) throws IOException
  {
    //System.out.println("deflate.write()");
    super.write(b, off, len);
    //available += len;
    //System.out.println("deflate.write(" + len + ")");
    //System.out.println("deflate.write(" + Arrays.toString(VTArrays.copyOfRange(b, off, len)) + ")");
  }
  
  public void finish() throws IOException
  {
    super.finish();
    //System.out.println("deflate.finish()");
  }
  
  public void flush() throws IOException
  {
    finish();
    out.flush();
    def.reset();
    //System.out.println("deflate.reset()");
  }
}
