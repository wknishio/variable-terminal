package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.VT;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.array.VTByteArrayOutputStream;

public final class VTPacketDecompressor extends OutputStream
{
  private static final int bufferSize = VT.VT_COMPRESSION_BUFFER_SIZE_BYTES;
  private InputStream in;
  private OutputStream out;
  private final byte[] buffer = new byte[bufferSize];
  private final byte[] single = new byte[1];
  private int readed;
  
  private VTByteArrayOutputStream outputBuffer = new VTByteArrayOutputStream(bufferSize);
  private VTByteArrayInputStream inputBuffer = new VTByteArrayInputStream(outputBuffer.buf());
  
  public VTPacketDecompressor(OutputStream out)
  {
    this.out = out;
  }
  
  public InputStream getCompressedPacketInputStream()
  {
    return inputBuffer;
  }
  
  public void setPacketDecompressorInputStream(InputStream in)
  {
    this.in = in;
  }
  
  //this should not be called
  public void write(int b) throws IOException
  {
    single[0] = (byte) b;
    write(single);
  }
  
  public void write(byte[] data) throws IOException
  {
    write(data, 0, data.length);
  }
  
  public void write(byte[] data, int off, int len) throws IOException
  {
    outputBuffer.count(0);
    outputBuffer.write(data, off, len);
    inputBuffer.pos(0);
    inputBuffer.count(outputBuffer.count());
    readed = 1;
    while (readed > 0 && inputBuffer.available() > 0)
    {
      readed = in.read(buffer, 0, bufferSize);
      if (readed > 0)
      {
        out.write(buffer, 0, readed);
      }
    }
  }
  
  public void flush() throws IOException
  {
    out.flush();
  }
  
  public void close() throws IOException
  {
    out.close();
  }
}
