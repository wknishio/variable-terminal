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
  private byte[] output = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
  private StreamCipher streamCipher;
  
  public VTStreamCipherOutputStream(OutputStream out, StreamCipher streamCipher)
  {
    super(out);
    this.streamCipher = streamCipher;
  }
  
  public void write(int input) throws IOException
  {
    single1[0] = (byte) input;
    streamCipher.processBytes(single1, 0, 1, single2, 0);
    out.write(single2);
  }
  
  public void write(byte[] input) throws IOException
  {
    int len = input.length;
    if (output.length < len)
    {
      output = new byte[len];
    }
    streamCipher.processBytes(input, 0, len, output, 0);
    out.write(output, 0, len);
  }
  
  public void write(byte[] input, int off, int len) throws IOException
  {
    if (output.length < len)
    {
      output = new byte[len];
    }
    streamCipher.processBytes(input, off, len, output, 0);
    out.write(output, 0, len);
  }
}