
package org.vash.vate.stream.pipe;

import java.io.InputStream;
import java.io.IOException;

public final class VTPipedInputStream extends InputStream
{
  //private static final int DEFAULT_BUFFER_SIZE = 8192;
  
  private VTPipedOutputStream source;
  private final byte[] circBuf;
  private int rOffset;
  private int wOffset;
  private boolean isWaitGet;
  private boolean isWaitPut;
  private boolean eof;
  private boolean closed;
  
  public VTPipedInputStream(int bufferSize)
  {
    this.circBuf = new byte[bufferSize];
    this.isWaitGet = false;
    this.isWaitPut = false;
    this.rOffset = 0;
    this.wOffset = 0;
  }
  
//  public VTPipedInputStream()
//  {
//    this(DEFAULT_BUFFER_SIZE);
//  }
  
  public VTPipedInputStream(VTPipedOutputStream source, int bufferSize) throws IOException
  {
    this(bufferSize);
    connect(source);
  }
  
  public synchronized final void connect(VTPipedOutputStream source) throws IOException
  {
    if (this.source == source)
    {
      return;
    }
    if (this.source != null)
    {
      throw new IOException("Pipe already connected");
    }
    this.source = source;
    source.connect(this);
  }
  
  public final synchronized int read() throws IOException
  {
    while (isEmpty())
    {
      if (closed)
      {
        throw new IOException("InputStreamPipe closed");
      }
      if (eof)
      {
        return -1;
      }
      isWaitGet = true;
      try
      {
        this.wait();
      }
      catch (InterruptedException e)
      {
        // !
        // throw new IOException(e);
      }
    }
    isWaitGet = false;
    
    int b = (circBuf[rOffset++] & 0xff);
    
    if (rOffset == circBuf.length)
      rOffset = 0;
    
    if (isWaitPut)
    {
      isWaitPut = false;
      this.notifyAll();
      //this.notifyAll();
    }
    
    return b;
  }
  
  public final synchronized int read(byte[] buf, int off, int len) throws IOException
  {
    while (isEmpty())
    {
      if (closed)
      {
        throw new IOException("InputStreamPipe closed");
      }
      if (eof)
      {
        return -1;
      }
      isWaitGet = true;
      try
      {
        this.wait();
      }
      catch (InterruptedException e)
      {
        // !
        // throw new IOException(e);
      }
    }
    isWaitGet = false;
    
    int n = available();
    n = Math.min(n, len);
    
    if (rOffset < wOffset)
    {
      System.arraycopy(circBuf, rOffset, buf, off, n);
    }
    else
    {
      int rest = circBuf.length - rOffset;
      if (rest < n)
      {
        System.arraycopy(circBuf, rOffset, buf, off, rest);
        System.arraycopy(circBuf, 0, buf, off + rest, n - rest);
      }
      else
      {
        System.arraycopy(circBuf, rOffset, buf, off, n);
      }
    }
    
    rOffset += n;
    if (rOffset >= circBuf.length)
      rOffset -= circBuf.length;
    
    if (isWaitPut)
    {
      isWaitPut = false;
      this.notifyAll();
      //this.notifyAll();
    }
    
    return n;
  }
  
  public final synchronized int available()
  {
    if (closed)
    {
      return -1;
    }
    return circBuf.length - freeSpace() - 1;
  }
  
  public final synchronized void close() throws IOException
  {
    closed = true;
    this.notifyAll();
  }
  
//  public final synchronized void flush()
//  {
//    this.notifyAll();
//  }
  
  protected final synchronized void put(int b) throws IOException
  {
    if (eof)
    {
      throw new IOException("InputStreamPipe already got eof");
    }
    if (closed)
    {
      throw new IOException("InputStreamPipe closed");
    }
    while (freeSpace() == 0)
    {
      if (eof)
      {
        throw new IOException("InputStreamPipe already got eof");
      }
      if (closed)
      {
        throw new IOException("InputStreamPipe closed");
      }
      isWaitPut = true;
      try
      {
        this.wait();
      }
      catch (InterruptedException e)
      {
        // !
      }
    }
    circBuf[wOffset++] = (byte) b;
    if (wOffset == circBuf.length)
      wOffset = 0;
    if (isWaitGet)
      this.notify();
  }
  
  protected final synchronized void put(byte[] buf, int off, int len) throws IOException
  {
    while (len > 0)
    {
      if (eof)
      {
        throw new IOException("InputStreamPipe already got eof");
      }
      if (closed)
      {
        throw new IOException("InputStreamPipe closed");
      }
      while (freeSpace() == 0)
      {
        if (eof)
        {
          throw new IOException("InputStreamPipe already got eof");
        }
        if (closed)
        {
          throw new IOException("InputStreamPipe closed");
        }
        isWaitPut = true;
        try
        {
          this.wait();
        }
        catch (InterruptedException e)
        {
          // !
        }
      }
      int n = freeSpace();
      n = (n > len ? len : n);
      
      if (wOffset < rOffset)
      {
        System.arraycopy(buf, off, circBuf, wOffset, n);
      }
      else
      {
        int rest = circBuf.length - wOffset;
        if (rest < n)
        {
          System.arraycopy(buf, off, circBuf, wOffset, rest);
          System.arraycopy(buf, off + rest, circBuf, 0, n - rest);
        }
        else
        {
          System.arraycopy(buf, off, circBuf, wOffset, n);
        }
      }
      
      wOffset += n;
      if (wOffset >= circBuf.length)
      {
        wOffset -= circBuf.length;
      }
      len -= n;
      off += n;
      
      if (isWaitGet)
        this.notify();
    }
  }
  
  protected final synchronized void eof()
  {
    eof = true;
    this.notify();
  }
  
  private final int freeSpace()
  {
    int fSpc = rOffset - wOffset;
    if (fSpc <= 0)
    {
      fSpc += circBuf.length;
    }
    fSpc--;
    return fSpc;
  }
  
  private final synchronized boolean isEmpty()
  {
    return (rOffset == wOffset) || closed;
  }
  
  /*
   * private void putFlowControl() throws IOException { while(freeSpace() == 0)
   * { if(eof) { throw new IOException("InputStreamPipe already got eof"); }
   * if(closed) { throw new IOException("InputStreamPipe closed"); } isWaitPut =
   * true; try { this.wait(); } catch (InterruptedException e) { // ! } } }
   */
  
  public final synchronized void open()
  {
    eof = false;
    closed = false;
    isWaitGet = false;
    isWaitPut = false;
    rOffset = 0;
    wOffset = 0;
    notifyAll();
  }
  
  public final synchronized boolean isClosed()
  {
    return closed;
  }
  
  public final synchronized boolean isEof()
  {
    return eof;
  }
  
  /*
   * public synchronized void waitOpen() { while (closed || eof) { try {
   * this.wait(); } catch (InterruptedException e) { } } }
   */
  
  /*
   * DEBUG/Test public static void main(String[] argv) { try { final
   * InputStreamPipe in = new InputStreamPipe(); final OutputStreamPipe out =
   * new OutputStreamPipe(in); //final java.io.PipedInputStream in = new
   * java.io.PipedInputStream(); //final java.io.PipedOutputStream out = new
   * java.io.PipedOutputStream(in); final byte[] msg = new byte[4711]; for(int i
   * = 0; i < 4711; i++) { msg[i] = (byte)((i i) ^ (i + i)); } Thread w = new
   * Thread(new Runnable() { public void run() { try { for(int i = 0; i < 1000;
   * i++) { out.write(msg); } } catch (IOException e) {
   * System.out.println("Error in w: " + e); e.printStackTrace(); } } }); Thread
   * r = new Thread(new Runnable() { public void run() { try { byte[] imsg = new
   * byte[4711]; for(int i = 0; i < 1000; i++) { int l = 4711; int o = 0;
   * while(o < 4711) { int n = in.read(imsg, o, l); o += n; l -= n; } } } catch
   * (IOException e) { System.out.println("Error in w: " + e);
   * e.printStackTrace(); } } }); long start = System.currentTimeMillis();
   * System.out.println("Start: " + (start / 1000)); w.start(); r.start();
   * r.join(); long now = System.currentTimeMillis(); System.out.println("End: "
   * + (now / 1000)); System.out.println("Lapsed: " + (now - start)); } catch
   * (Throwable e) { System.out.println("Error: " + e); e.printStackTrace(); } }
   */
}