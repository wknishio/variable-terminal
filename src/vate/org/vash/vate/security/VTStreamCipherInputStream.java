package org.vash.vate.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.StreamCipher;

public class VTStreamCipherInputStream extends FilterInputStream
{
  private final byte[] input = new byte[1];
  private final byte[] encrypted;
  private StreamCipher streamCipher;
  
  public VTStreamCipherInputStream(InputStream in, StreamCipher streamCipher, int bufferSize)
  {
    super(in);
    this.streamCipher = streamCipher;
    this.encrypted = new byte[bufferSize];
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
    return streamCipher.processBytes(encrypted, 0, in.read(encrypted, 0, decrypted.length), decrypted, 0);
  }
  
  public int read(final byte[] decrypted, final int off, final int len) throws IOException
  {
    return streamCipher.processBytes(encrypted, 0, in.read(encrypted, 0, len), decrypted, off);
  }
}