package org.vate.stream.array;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class VTByteArrayOutputStream extends ByteArrayOutputStream
{
  public VTByteArrayOutputStream()
  {
    super();
  }

  public VTByteArrayOutputStream(int size)
  {
    super(size);
  }

  public final byte[] buf()
  {
    return this.buf;
  }

  public final int count()
  {
    return this.count;
  }

  public final void buf(byte[] buf)
  {
    this.buf = buf;
  }

  public final void count(int count)
  {
    this.count = count;
  }

  public final void close() throws IOException
  {
    // super.close();
  }
}