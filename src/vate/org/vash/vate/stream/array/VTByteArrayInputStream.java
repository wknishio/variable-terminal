package org.vash.vate.stream.array;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

public final class VTByteArrayInputStream extends ByteArrayInputStream
{
  public VTByteArrayInputStream(byte[] buf)
  {
    super(buf);
  }
  
  public VTByteArrayInputStream(byte[] buf, int offset, int length)
  {
    super(buf, offset, length);
  }
  
  public final byte[] buf()
  {
    return this.buf;
  }
  
  public final int count()
  {
    return this.count;
  }
  
  public final int mark()
  {
    return this.mark;
  }
  
  public final int pos()
  {
    return this.pos;
  }
  
  public final void buf(byte[] buf)
  {
    this.buf = buf;
  }
  
  public final void buf(byte[] buf, int count)
  {
    this.buf = buf;
    this.count = count;
  }
  
  public final void count(int count)
  {
    this.count = count;
  }
  
  public final void pos(int pos)
  {
    this.pos = pos;
  }
  
  public final int readByte() throws IOException
  {
    int b = read();
    if (b == -1)
    {
      throw new EOFException();
    }
    return b;
  }
}