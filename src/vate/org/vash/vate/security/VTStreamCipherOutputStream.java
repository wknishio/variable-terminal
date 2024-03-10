package org.vash.vate.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherOutputStream extends FilterOutputStream
{
  private final byte[] single1 = new byte[1];
  private final byte[] single2 = new byte[1];
  private byte[] buffer = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherOutputStream(OutputStream out, StreamCipher streamCipher)
  {
    super(out);
    this.streamCipher = streamCipher;
  }
  
  public void write(final int input) throws IOException
  {
    single1[0] = (byte) input;
    streamCipher.processBytes(single1, 0, 1, single2, 0);
    out.write(single2);
  }
  
  public void write(final byte[] input) throws IOException
  {
    if (buffer.length < input.length)
    {
      buffer = new byte[input.length];
    }
    streamCipher.processBytes(input, 0, input.length, buffer, 0);
    out.write(buffer, 0, input.length);
  }
  
  public void write(final byte[] input, final int off, final int len) throws IOException
  {
    if (buffer.length < len)
    {
      buffer = new byte[len];
    }
    streamCipher.processBytes(input, off, len, buffer, 0);
    out.write(buffer, 0, len);
  }
}