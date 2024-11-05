package org.vash.vate.stream.compress;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import org.vash.vate.stream.endian.VTLittleEndianInputStream;

public class VTNoFlushInflaterInputStream extends InflaterInputStream
{
  private VTLittleEndianInputStream lin;
  
  public VTNoFlushInflaterInputStream(InputStream in, Inflater inflater, int size)
  {
    super(in, inflater, size);
    this.lin = new VTLittleEndianInputStream(in);
  }
  
  public int available() throws IOException
  {
    if (inf.needsInput())
    {
      return 0;
    }
    else
    {
      return 1;
    }
  }
  
  protected void fill() throws IOException
  {
    int len = lin.readInt();
    if (buf.length < len)
    {
      buf = new byte[len];
    }
    if (len == -1)
    {
      throw new EOFException("Unexpected end of ZLIB input stream");
    }
    lin.readFully(buf, 0, len);
    inf.setInput(buf, 0, len);
  }
  
  public int read(byte[] b, int off, int len) throws IOException
  {
    if (b == null)
    {
      throw new NullPointerException();
    }
    else if (off < 0 || len < 0 || len > b.length - off)
    {
      throw new IndexOutOfBoundsException();
    }
    else if (len == 0)
    {
      return 0;
    }
    try
    {
      int n;
      while ((n = inf.inflate(b, off, len)) == 0)
      {
        if (inf.finished() || inf.needsDictionary())
        {
          inf.reset();
        }
        if (inf.needsInput())
        {
          fill();
        }
      }
      return n;
    }
    catch (DataFormatException e)
    {
      String s = e.getMessage();
      throw new ZipException(s != null ? s : "Invalid ZLIB data format");
    }
  }
}
