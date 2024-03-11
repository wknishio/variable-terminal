package org.vash.vate.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherOutputStream extends FilterOutputStream
{
  private final byte[] input = new byte[1];
  private final byte[] output = new byte[1];
  private byte[] encrypted = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherOutputStream(OutputStream out, StreamCipher streamCipher)
  {
    super(out);
    this.streamCipher = streamCipher;
  }
  
  public void write(final int decrypted) throws IOException
  {
    input[0] = (byte) decrypted;
    streamCipher.processBytes(input, 0, 1, output, 0);
    out.write(output);
  }
  
  public void write(final byte[] decrypted) throws IOException
  {
    if (encrypted.length < decrypted.length)
    {
      encrypted = new byte[decrypted.length];
    }
    out.write(encrypted, 0, streamCipher.processBytes(decrypted, 0, decrypted.length, encrypted, 0));
  }
  
  public void write(final byte[] decrypted, final int off, final int len) throws IOException
  {
    if (encrypted.length < len)
    {
      encrypted = new byte[len];
    }
    out.write(encrypted, 0, streamCipher.processBytes(decrypted, off, len, encrypted, 0));
  }
}