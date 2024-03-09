package org.vash.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBufferedOutputStream extends FilterOutputStream
{
  /** The internal buffer where data is stored. */
  protected final byte[] buf;
  
  /**
   * The number of valid bytes in the buffer. This value is always in the range
   * <tt>0</tt> through <tt>buf.length</tt>; elements <tt>buf[0]</tt> through
   * <tt>buf[count-1]</tt> contain valid byte data.
   */
  protected int count;
  
  private boolean flushWhenFull;
  
  /**
   * Creates a new buffered output stream to write data to the specified
   * underlying output stream.
   *
   * @param out
   *          the underlying output stream.
   */
  public VTBufferedOutputStream(OutputStream out, boolean flushWhenFull)
  {
    this(out, 8192, flushWhenFull);
  }
  
  /**
   * Creates a new buffered output stream to write data to the specified
   * underlying output stream with the specified buffer size.
   *
   * @param out
   *          the underlying output stream.
   * @param size
   *          the buffer size.
   * @exception IllegalArgumentException
   *              if size &lt;= 0.
   */
  public VTBufferedOutputStream(OutputStream out, int size, boolean flushWhenFull)
  {
    super(out);
    this.flushWhenFull = flushWhenFull;
    if (size <= 0)
    {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    buf = new byte[size];
  }
  
  /** Flush the internal buffer */
  private final void flushBuffer(boolean full) throws IOException
  {
    if (count > 0)
    {
      out.write(buf, 0, count);
      count = 0;
      if (flushWhenFull && full)
      {
        out.flush();
      }
    }
  }
  
  /**
   * Writes the specified byte to this buffered output stream.
   *
   * @param b
   *          the byte to be written.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public synchronized final void write(int b) throws IOException
  {
    if (count >= buf.length)
    {
      flushBuffer(true);
    }
    buf[count++] = (byte) b;
  }
  
  /**
   * Writes <code>len</code> bytes from the specified byte array starting at
   * offset <code>off</code> to this buffered output stream.
   * <p>
   * Ordinarily this method stores bytes from the given array into this stream's
   * buffer, flushing the buffer to the underlying output stream as needed. If
   * the requested length is at least as large as this stream's buffer, however,
   * then this method will flush the buffer and write the bytes directly to the
   * underlying output stream. Thus redundant <code>BufferedOutputStream</code>s
   * will not copy data unnecessarily.
   *
   * @param b
   *          the data.
   * @param off
   *          the start offset in the data.
   * @param len
   *          the number of bytes to write.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public synchronized final void write(byte b[], int off, int len) throws IOException
  {
    if (len >= buf.length)
    {
      /*
       * If the request length exceeds the size of the output buffer, flush the
       * output buffer and then write the data directly. In this way buffered
       * streams will cascade harmlessly.
       */
      flushBuffer(true);
      out.write(b, off, len);
      return;
    }
    int spaceLeft = buf.length - count;
    if (len > spaceLeft)
    {
      // Fill the buffer to the max before flushing
      System.arraycopy(b, off, buf, count, spaceLeft);
      count += spaceLeft;
      flushBuffer(true);
      int remaining = len - spaceLeft;
      System.arraycopy(b, off + spaceLeft, buf, count, remaining);
      count += remaining;
    }
    else
    {
      System.arraycopy(b, off, buf, count, len);
      count += len;
    }
  }
  
  /**
   * Flushes this buffered output stream. This forces any buffered output bytes
   * to be written out to the underlying output stream.
   *
   * @exception IOException
   *              if an I/O error occurs.
   * @see java.io.FilterOutputStream#
   */
  public synchronized final void flush() throws IOException
  {
    flushBuffer(false);
    out.flush();
  }
  
  public synchronized final void close() throws IOException
  {
    try
    {
      flush();
    }
    catch (Throwable t)
    {
      
    }
    out.close();
  }
}
