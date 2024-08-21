package org.vash.vate.socket.managed;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.vash.vate.ping.VTNanoPingListener;

public class VTManagedSocketPingListener implements VTNanoPingListener, Callable<Boolean>
{
  private ExecutorService executor;
  private VTManagedConnection connection;
  
  public VTManagedSocketPingListener(ExecutorService executor, VTManagedConnection connection)
  {
    this.executor = executor;
    this.connection = connection;
  }
  
  public void pingObtained(long nanoDelay)
  {
    synchronized (this)
    {
      notifyAll();
    }
  }
  
  public boolean ping(long timeout)
  {
    boolean result = false;
    try
    {
      result = executor.submit(this).get(timeout, TimeUnit.MILLISECONDS);
    } 
    catch (Throwable e)
    {
      
    }
    synchronized (this)
    {
      notifyAll();
    }
    return result;
  }
  
  public Boolean call() throws Exception
  {
    synchronized (this)
    {
      connection.ping();
      wait(0);
    }
    return true;
  }
}