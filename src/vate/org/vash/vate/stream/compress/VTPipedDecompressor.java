package org.vash.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vash.vate.VT;
import org.vash.vate.stream.array.VTCircularByteBuffer;

public final class VTPipedDecompressor extends OutputStream
{
  private static final int bufferSize = VT.VT_COMPRESSED_DATA_BUFFER_SIZE;
  private InputStream in;
  private OutputStream out;
  private byte[] buffer = new byte[bufferSize];
  // private VTPipedInputStream pipedInputStream;
  // private VTPipedOutputStream pipedOutputStream;

  // private VTByteArrayOutputStream outputBuffer = new
  // VTByteArrayOutputStream(bufferSize);
  // private VTByteArrayInputStream inputBuffer = new
  // VTByteArrayInputStream(outputBuffer.buf());
  private VTCircularByteBuffer circularBuffer;

  public VTPipedDecompressor(OutputStream out)
  {
    this.out = out;
    this.circularBuffer = new VTCircularByteBuffer(bufferSize);
    // this.pipedInputStream = new VTPipedInputStream(bufferSize);
    // this.pipedOutputStream = new VTPipedOutputStream();
    // try
    // {
    // this.pipedInputStream.connect(this.pipedOutputStream);
    // }
    // catch (IOException e)
    // {

    // }
  }

  public InputStream getPipedInputStream()
  {
    return circularBuffer.getInputStream();
  }

  public void setDecompressor(InputStream in)
  {
    this.in = in;
  }

  public void write(int b) throws IOException
  {
    write(new byte[] { (byte) b });
  }

  public void write(byte[] data) throws IOException
  {
    write(data, 0, data.length);
  }

  public void write(byte[] data, int off, int len) throws IOException
  {
    // System.out.println("compressed:["+len+"]");
    circularBuffer.getOutputStream().write(data, off, len);
    circularBuffer.getOutputStream().flush();
    // int available = 0;
    while ((circularBuffer.getInputStream().available()) > 0)
    {
      out.write(buffer, 0, in.read(buffer, 0, bufferSize));
      out.flush();
    }
    // System.out.println("compressed data:" + len);
    // System.out.println("decompressed data:" + readed);
    // out.write(data, off, len);
  }

  public void flush() throws IOException
  {
    out.flush();
  }

  public void close() throws IOException
  {
    // flush();
    out.close();
  }
}
