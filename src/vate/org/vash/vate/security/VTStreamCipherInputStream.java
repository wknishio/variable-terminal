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
  private byte[] input = new byte[VT.VT_STANDARD_DATA_BUFFER_SIZE];
  private StreamCipher cipher;
  
  public VTStreamCipherInputStream(InputStream in, StreamCipher cipher)
  {
    super(in);
    this.cipher = cipher;
  }

  public int read() throws IOException
  {
    single1[0] = (byte) in.read();
    cipher.processBytes(single1, 0, 1, single2, 0);
    return single2[0];
  }
  
  public int read(byte[] output, int off, int len) throws IOException
  {
    int readed = in.read(input, off, len);
    cipher.processBytes(input, off, readed, output, off);
    return readed;
  }
}