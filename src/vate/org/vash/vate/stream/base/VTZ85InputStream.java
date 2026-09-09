package org.vash.vate.stream.base;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.vash.vate.sheepy.util.text.Base85.Z85Decoder;

public class VTZ85InputStream extends FilterInputStream
{
  private DataInputStream input;
  private Z85Decoder decoder = new Z85Decoder();
  
  public VTZ85InputStream(InputStream in)
  {
    super(in);
    this.input = new DataInputStream(in);
  }
  
  public int read(byte[] buf, int off, int len) throws IOException
  {
    readFully(buf, off, len);
    return len;
  }
  
  public int read(byte[] buf) throws IOException
  {
    readFully(buf);
    return buf.length;
  }
  
  private void readFully(byte[] buf) throws IOException
  {
    byte[] encoded = new byte[buf.length + (buf.length >> 2)];
    input.readFully(encoded);
    System.arraycopy(decoder.decode(encoded), 0, buf, 0, buf.length);
  }
  
  private void readFully(byte[] buf, int off, int len) throws IOException
  {
    byte[] encoded = new byte[len + (len >> 2)];
    input.readFully(encoded);
    System.arraycopy(decoder.decode(encoded), 0, buf, off, len);
  }
}