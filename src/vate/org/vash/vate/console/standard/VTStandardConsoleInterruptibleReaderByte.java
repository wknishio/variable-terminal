package org.vash.vate.console.standard;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.vash.vate.compatibility.VTArrays;

public class VTStandardConsoleInterruptibleReaderByte implements Runnable
{
  private boolean requested;
  // private BufferedReader standardTerminalReader;
  private final byte[] readBuffer = new byte[8192];
  private BlockingQueue<byte[]> buffer;
  // private static final String ANSIDetectionPattern =
  // "(\\u001B)(\\[)([^R])*([R])";
  private Executor executor;
  
  public VTStandardConsoleInterruptibleReaderByte()
  {
    // String testString = "\u001B[1;1eeR";
    // System.out.println("matches:" +
    // testString.matches(ANSIDetectionPattern));
    this.buffer = new LinkedBlockingQueue<byte[]>();
    this.executor = Executors.newFixedThreadPool(1, new ThreadFactory()
    {
      public Thread newThread(Runnable r)
      {
        Thread created = new Thread(null, r, r.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    // standardTerminalReader = new BufferedReader(new
    // VTStandardConsoleNativeReader());
  }
  
  public void setEcho(boolean echo)
  {
    // this.echo = echo;
  }
  
  public synchronized byte[] read() throws InterruptedException
  {
    if (buffer.size() == 0)
    {
      if (requested == false)
      {
        requested = true;
        executor.execute(this);
      }
    }
    byte[] data = buffer.take();
    requested = false;
    if (data != null)
    {
      return data;
    }
    return null;
  }
  
  public void run()
  {
    int readedBytes = 0;
    try
    {
      readedBytes = System.in.read(readBuffer, 0, readBuffer.length);
      buffer.offer(VTArrays.copyOf(readBuffer, readedBytes));
    }
    catch (Throwable t)
    {
      
    }
    requested = false;
  }
}