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
  private final InputStream compressedInputStream;
  private final OutputStream decompressedOutputStream;
  private final VTByteArrayOutputStream packetOutputPipe;
  private final VTByteArrayInputStream packetInputPipe;
  private final byte[] buffer = new byte[bufferSize];
  private final byte[] single = new byte[1];
  private int readed;
  
  public VTPacketDecompressor(final OutputStream decompressedOutputStream, final InputStream compressedInputStream, final VTByteArrayOutputStream packetOutputPipe, final VTByteArrayInputStream packetInputPipe)
  {
    this.decompressedOutputStream = decompressedOutputStream;
    this.compressedInputStream = compressedInputStream;
    this.packetOutputPipe = packetOutputPipe;
    this.packetInputPipe = packetInputPipe;
  }
  
  //this should not be called
  public void write(final int b) throws IOException
  {
    single[0] = (byte) b;
    write(single);
  }
  
  public void write(final byte[] data) throws IOException
  {
    write(data, 0, data.length);
  }
  
  public void write(final byte[] data, final int off, final int len) throws IOException
  {
    packetOutputPipe.count(0);
    packetOutputPipe.write(data, off, len);
    packetInputPipe.pos(0);
    packetInputPipe.count(packetOutputPipe.count());
    readed = 1;
    while (readed > 0 && packetInputPipe.available() > 0)
    {
      readed = compressedInputStream.read(buffer, 0, bufferSize);
      if (readed > 0)
      {
        decompressedOutputStream.write(buffer, 0, readed);
      }
    }
  }
  
  public void flush() throws IOException
  {
    decompressedOutputStream.flush();
  }
  
  public void close() throws IOException
  {
    decompressedOutputStream.close();
  }
}
