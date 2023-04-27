package org.vash.vate.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.VT;

public class VTStreamCipherOutputStream extends FilterOutputStream
{
  private byte[] single1 = new byte[0];
  private byte[] single2 = new byte[0];
  private StreamCipher cipher;
  private byte[] output = new byte[VT.VT_STANDARD_DATA_BUFFER_SIZE];
  
  public VTStreamCipherOutputStream(OutputStream out, StreamCipher cipher)
  {
    super(out);
    this.cipher = cipher;
  }
  
  public void write(byte[] input, int off, int len) throws IOException
  {
    cipher.processBytes(input, off, len, output, off);
    out.write(output, off, len);
  }
  
  public void write(int input) throws IOException
  {
    single1[0] = (byte) input;
    cipher.processBytes(single1, 0, 1, single2, 0);
    out.write(single2);
  }
}