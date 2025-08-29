package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.VTSystem;
import org.vash.vate.stream.array.VTByteArrayInputStream;

public final class VTPacketDecompressor extends OutputStream
{
  private static final int bufferSize = VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES;
  private final InputStream compressedInputStream;
  private final OutputStream decompressedOutputStream;
  private final VTByteArrayInputStream packetInputPipe;
  //private final VTByteArrayOutputStream packetOutputPipe;
  private final byte[] buffer = new byte[bufferSize];
  private final byte[] single = new byte[1];
  private int readed;
  
  public VTPacketDecompressor(final InputStream compressedInputStream, final OutputStream decompressedOutputStream, final VTByteArrayInputStream packetInputPipe)
  {
    this.compressedInputStream = compressedInputStream;
    this.decompressedOutputStream = decompressedOutputStream;
    this.packetInputPipe = packetInputPipe;
    //this.packetOutputPipe = packetOutputPipe;
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
    packetInputPipe.buf(data, off, len);
    readed = compressedInputStream.read(buffer, 0, bufferSize);
    decompressedOutputStream.write(buffer, 0, readed);
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