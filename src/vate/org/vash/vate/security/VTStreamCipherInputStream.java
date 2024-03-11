package org.vash.vate.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherInputStream extends FilterInputStream
{
  private final byte[] input = new byte[1];
  private byte[] encrypted = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherInputStream(InputStream in, StreamCipher streamCipher)
  {
    super(in);
    this.streamCipher = streamCipher;
  }
  
  public int read() throws IOException
  {
    int readed = read(input);
    if (readed < 0)
    {
      return readed;
    }
    return input[0];
  }
  
  public int read(final byte[] decrypted) throws IOException
  {
    if (encrypted.length < decrypted.length)
    {
      encrypted = new byte[decrypted.length];
    }
    return streamCipher.processBytes(encrypted, 0, in.read(encrypted, 0, decrypted.length), decrypted, 0);
  }
  
  public int read(final byte[] decrypted, final int off, final int len) throws IOException
  {
    if (encrypted.length < len)
    {
      encrypted = new byte[len];
    }
    return streamCipher.processBytes(encrypted, 0, in.read(encrypted, 0, len), decrypted, off);
  }
}