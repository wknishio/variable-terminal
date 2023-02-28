package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;

import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;

import io.airlift.compress.Decompressor;

public class VTAirliftInputStream extends InputStream
{
  private Decompressor decompressor;
  private VTLittleEndianInputStream in;
  private byte[] input = new byte[1];
  private VTByteArrayInputStream stream = new VTByteArrayInputStream(new byte[] {});
  private volatile boolean closed = false;
  
  public VTAirliftInputStream(InputStream in, Decompressor decompressor)
  {
    this.in = new VTLittleEndianInputStream(in);
    this.decompressor = decompressor;
  }
  
  public synchronized int available() throws IOException
  {
    return stream.available();
  }
  
  public synchronized int read(byte[] data, int off, int len) throws IOException
  {
    if (closed)
    {
      return -1;
    }
    if (stream.available() == 0)
    {
      readBlock();
    }
    if (stream.available() > 0)
    {
      return stream.read(data, off, len);
    }
    return -1;
  }
  
  public synchronized int read() throws IOException
  {
    if (closed)
    {
      return -1;
    }
    if (stream.available() == 0)
    {
      readBlock();
    }
    if (stream.available() > 0)
    {
      return stream.read();
    }
    return -1;
  }
  
  private synchronized void readBlock() throws IOException
  {
    int compressed = 0;
    int decompressed = 0;
    int max = 0;
    
    compressed = in.readInt();
    decompressed = in.readInt();
    max = in.readInt();
    if (input.length < compressed)
    {
      input = new byte[compressed];
    }
    if (stream.buf().length < max)
    {
      stream.buf(new byte[max]);
    }
    in.readFully(input, 0, compressed);
    decompressor.decompress(input, 0, compressed, stream.buf(), 0, max);
    stream.count(decompressed);
    stream.pos(0);
  }
  
  public synchronized void close() throws IOException
  {
    in.close();
    stream.count(0);
    stream.pos(0);
    closed = true;
  }
}
