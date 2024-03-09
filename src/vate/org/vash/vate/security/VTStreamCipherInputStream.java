package org.vash.vate.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherInputStream extends FilterInputStream
{
  private final byte[] single1 = new byte[1];
  private byte[] buffer = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherInputStream(InputStream in, StreamCipher streamCipher)
  {
    super(in);
    this.streamCipher = streamCipher;
  }
  
  public int read() throws IOException
  {
    int readed = read(single1);
    if (readed < 0)
    {
      return readed;
    }
    return single1[0];
  }
  
  public int read(byte[] output) throws IOException
  {
    if (buffer.length < output.length)
    {
      buffer = new byte[output.length];
    }
    return streamCipher.processBytes(buffer, 0, in.read(buffer, 0, output.length), output, 0);
  }
  
  public int read(byte[] output, int off, int len) throws IOException
  {
    if (buffer.length < len)
    {
      buffer = new byte[len];
    }
    return streamCipher.processBytes(buffer, 0, in.read(buffer, 0, len), output, off);
  }
}