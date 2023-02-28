package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;

import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

import io.airlift.compress.Compressor;

public class VTAirliftOutputStream extends OutputStream
{
  private Compressor compressor;
  private VTLittleEndianOutputStream out;
  private byte[] output = new byte[1];
  private byte[] single = new byte[1];
  
  public VTAirliftOutputStream(OutputStream out, Compressor compressor)
  {
    this.out = new VTLittleEndianOutputStream(out);
    this.compressor = compressor;
  }
  
  public synchronized void write(byte[] data, int off, int len) throws IOException
  {
    if (len > 0)
    {
      writeBlock(data, off, len);
    }
  }
  
  public synchronized void write(int data) throws IOException
  {
    single[0] = (byte) data;
    write(single, 0, 1);
  }
  
  private synchronized void writeBlock(byte[] data, int off, int len) throws IOException
  {
    int compressed = 0;
    int decompressed = 0;
    int max = 0;
    
    decompressed = len;
    max = compressor.maxCompressedLength(decompressed);
    if (output.length < max)
    {
      output = new byte[max];
    }
    compressed = compressor.compress(data, off, decompressed, output, 0, max);
    // max = Math.max(compressed, max);
    // compressed = Math.min(compressed, max);
    out.writeInt(compressed);
    out.writeInt(decompressed);
    out.writeInt(max);
    out.write(output, 0, compressed);
  }
  
  public synchronized void flush() throws IOException
  {
    out.flush();
  }
  
  public synchronized void close() throws IOException
  {
    out.close();
  }
}
