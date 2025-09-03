package org.vash.vate.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.vtbouncycastle.crypto.StreamCipher;

public class VTStreamCipherOutputStream extends FilterOutputStream
{
  private final byte[] input = new byte[1];
  private final byte[] output = new byte[1];
  private final byte[] encrypted;
  private StreamCipher streamCipher;
  
  public VTStreamCipherOutputStream(OutputStream out, StreamCipher streamCipher, int bufferSize)
  {
    super(out);
    this.streamCipher = streamCipher;
    this.encrypted = new byte[bufferSize];
  }
  
  public void write(final int decrypted) throws IOException
  {
    input[0] = (byte) decrypted;
    streamCipher.processBytes(input, 0, 1, output, 0);
    out.write(output);
  }
  
  public void write(final byte[] decrypted) throws IOException
  {
    out.write(encrypted, 0, streamCipher.processBytes(decrypted, 0, decrypted.length, encrypted, 0));
  }
  
  public void write(final byte[] decrypted, final int off, final int len) throws IOException
  {
    out.write(encrypted, 0, streamCipher.processBytes(decrypted, off, len, encrypted, 0));
  }
}