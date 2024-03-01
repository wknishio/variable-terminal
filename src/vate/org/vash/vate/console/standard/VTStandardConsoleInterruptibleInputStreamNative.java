package org.vash.vate.console.standard;

import java.io.IOException;

import org.vash.vate.VT;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public class VTStandardConsoleInterruptibleInputStreamNative extends VTStandardConsoleInterruptibleInputStream
{
  private VTPipedInputStream inputPipe;
  private VTPipedOutputStream outputPipe;
  // private VTByteArrayOutputStream lineBuffer;
  private VTStandardConsoleInterruptibleReaderNative reader;
  private Thread currentThread;
  
  public VTStandardConsoleInterruptibleInputStreamNative()
  {
    try
    {
      this.inputPipe = new VTPipedInputStream(VT.VT_REDUCED_BUFFER_SIZE_BYTES);
      this.outputPipe = new VTPipedOutputStream();
      this.outputPipe.connect(inputPipe);
    }
    catch (Throwable e)
    {
      
    }
    // this.lineBuffer = new VTByteArrayOutputStream();
    this.reader = new VTStandardConsoleInterruptibleReaderNative();
    // Thread readerThread = new Thread(reader,
    // "VTStandardTerminalInterruptibleReader");
    // readerThread.setDaemon(true);
    // readerThread.start();
  }
  
  public void setEcho(boolean echo)
  {
    reader.setEcho(echo);
  }
  
  public boolean usingRead()
  {
    return currentThread == null;
  }
  
  public void interruptRead()
  {
    if (currentThread != null)
    {
      try
      {
        currentThread.interrupt();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  /*
   * public String readLine() throws IOException, InterruptedException {
   * lineBuffer.count(0); int data = read(); while (data != -1 && data != '\n')
   * { lineBuffer.write(data); data = interruptibleRead(); } return new
   * String(lineBuffer.toByteArray()); }
   */
  
  /*
   * public int interruptibleRead() throws IOException, InterruptedException {
   * currentThread = Thread.currentThread(); if (inputPipe.available() == 0) {
   * String line = null; while (line == null || line.length() == 0) { line =
   * reader.read(); } outputPipe.write(line.getBytes()); outputPipe.flush(); }
   * return inputPipe.read(); }
   */
  
  public int read() throws IOException
  {
    currentThread = Thread.currentThread();
    if (inputPipe.available() == 0)
    {
      try
      {
        String line = null;
        while (line == null || line.length() == 0)
        {
          line = reader.read();
        }
        outputPipe.write(line.getBytes("UTF-8"));
        outputPipe.flush();
      }
      catch (InterruptedException e)
      {
        throw new IOException(e.getMessage());
      }
      finally
      {
        currentThread = null;
      }
    }
    return inputPipe.read();
  }
  
  public int read(byte[] buf, int off, int len) throws IOException
  {
    buf[off] = (byte) read();
    return 1;
  }
  
  public int available() throws IOException
  {
    return inputPipe.available();
  }
  
  public void close() throws IOException
  {
    // unclosable!
  }
}