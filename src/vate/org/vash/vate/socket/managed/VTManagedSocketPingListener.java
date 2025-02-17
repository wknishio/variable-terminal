package org.vash.vate.socket.managed;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.vash.vate.ping.VTNanoPingListener;

public class VTManagedSocketPingListener implements VTNanoPingListener, Callable<Long>
{
  private final ExecutorService executor;
  private final VTManagedConnection connection;
  private volatile Long pingResult = -1L;
  
  public VTManagedSocketPingListener(ExecutorService executor, VTManagedConnection connection)
  {
    this.executor = executor;
    this.connection = connection;
  }
  
  public void pingObtained(long nanoDelay)
  {
    synchronized (this)
    {
      pingResult = nanoDelay;
      notifyAll();
    }
  }
  
  public long checkPing(long timeoutNanoSeconds)
  {
    synchronized (this)
    {
      if (pingResult == null)
      {
        pingResult = -1L;
      }
      notifyAll();
    }
    long result = -1;
    try
    {
      if (timeoutNanoSeconds > 0)
      {
        result = executor.submit(this).get(timeoutNanoSeconds, TimeUnit.NANOSECONDS);
      }
      else
      {
        result = executor.submit(this).get();
      }
    } 
    catch (Throwable e)
    {
      
    }
    return result;
  }
  
  public long checkPing()
  {
    return checkPing(0);
  }
  
  public Long call() throws Exception
  {
    synchronized (this)
    {
      pingResult = null;
      connection.requestPing();
      while (pingResult == null)
      {
        wait(0);
      }
      return pingResult;
    }
  }
}