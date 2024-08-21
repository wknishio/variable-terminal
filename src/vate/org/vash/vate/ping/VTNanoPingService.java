package org.vash.vate.ping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.task.VTTask;

public class VTNanoPingService extends VTTask
{
  private boolean server;
  // private int initial;
  private int interval;
  private VTLittleEndianInputStream in;
  private VTLittleEndianOutputStream out;
  private Queue<VTNanoPingListener> listeners;
  private long startNanoTime = 0;
  private long endNanoTime = 0;
  private long localNanoDelay = 0;
  private long remoteNanoDelay = 0;
  
  public VTNanoPingService(int interval, boolean server, ExecutorService executorService)
  {
    super(executorService);
    this.listeners = new ConcurrentLinkedQueue<VTNanoPingListener>();
    // this.initial = initial;
    this.interval = interval;
    this.server = server;
  }
  
  public void ping()
  {
    try
    {
      synchronized (this)
      {
        this.notify();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public boolean addListener(VTNanoPingListener listener)
  {
    return listeners.add(listener);
  }
  
  public boolean removeListener(VTNanoPingListener listener)
  {
    return listeners.remove(listener);
  }
  
  public void setInputStream(InputStream in)
  {
    this.in = new VTLittleEndianInputStream(in);
  }
  
  public void setOutputStream(OutputStream out)
  {
    this.out = new VTLittleEndianOutputStream(out);
  }
  
  private void first() throws IOException, InterruptedException
  {
    // first cycle has no delays and is just to warmup
    if (!stopped)
    {
      // start timer
      startNanoTime = System.nanoTime();
      // startNanoTime = System.currentTimeMillis();
      out.writeLong(localNanoDelay);
      out.flush();
      remoteNanoDelay = in.readLong();
      // end timer
      endNanoTime = System.nanoTime();
      // endNanoTime = System.currentTimeMillis();
      if (endNanoTime >= startNanoTime)
      {
        localNanoDelay = endNanoTime - startNanoTime;
      }
      // wait interval
    }
  }
  
  private void client() throws IOException, InterruptedException
  {
    while (!stopped)
    {
      // start timer
      startNanoTime = System.nanoTime();
      // startNanoTime = System.currentTimeMillis();
      out.writeLong(localNanoDelay);
      out.flush();
      remoteNanoDelay = in.readLong();
      // end timer
      endNanoTime = System.nanoTime();
      // endNanoTime = System.currentTimeMillis();
      if (endNanoTime >= startNanoTime)
      {
        localNanoDelay = endNanoTime - startNanoTime;
      }
      for (VTNanoPingListener listener : listeners)
      {
        listener.pingObtained(localNanoDelay);
      }
      // wait interval
      synchronized (this)
      {
        this.wait(interval);
      }
    }
  }
  
  private void server() throws IOException, InterruptedException
  {
    while (!stopped)
    {
      remoteNanoDelay = in.readLong();
      out.writeLong(localNanoDelay);
      out.flush();
      for (VTNanoPingListener listener : listeners)
      {
        listener.pingObtained(remoteNanoDelay);
      }
    }
  }
  
  public void task()
  {
    try
    {
      if (!server)
      {
        first();
        client();
      }
      else
      {
        server();
      }
    }
    catch (Throwable e)
    {
      
    }
  }
}