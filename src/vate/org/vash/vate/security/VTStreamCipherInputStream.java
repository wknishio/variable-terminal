package org.vash.vate.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherInputStream extends FilterInputStream
{
  private byte[] single1 = new byte[0];
  private byte[] single2 = new byte[0];
  private byte[] input = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherInputStream(InputStream in, StreamCipher streamCipher)
  {
    super(in);
    this.streamCipher = streamCipher;
  }
  
  public int read() throws IOException
  {
    single1[0] = (byte) in.read();
    streamCipher.processBytes(single1, 0, 1, single2, 0);
    return single2[0];
  }
  
  public int read(byte[] output) throws IOException
  {
    int len = output.length;
    if (input.length < len)
    {
      input = new byte[len];
    }
    int readed = in.read(input, 0, len);
    return streamCipher.processBytes(input, 0, readed, output, 0);
  }
  
  public int read(byte[] output, int off, int len) throws IOException
  {
    if (input.length < len)
    {
      input = new byte[len];
    }
    int readed = in.read(input, 0, len);
    return streamCipher.processBytes(input, 0, readed, output, off);
  }
}