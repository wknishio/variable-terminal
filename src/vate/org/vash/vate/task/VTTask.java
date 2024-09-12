package org.vash.vate.task;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class VTTask implements Runnable, Closeable
{
  private volatile boolean stopped;
  private Future<?> taskThread;
  //private Thread nextThread;
  private Runnable next;
  private ExecutorService executorService;
  
  public VTTask(ExecutorService executorService)
  {
    this.executorService = executorService;
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public void interruptThread()
  {
    if (taskThread != null)
    {
      taskThread.cancel(true);
    }
  }
  
  @SuppressWarnings("deprecation")
  public void stopThread()
  {
    if (taskThread != null)
    {
      try
      {
        // taskThread.stop();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void joinThread()
  {
    if (taskThread != null)
    {
      try
      {
        taskThread.get();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public boolean aliveThread()
  {
    if (taskThread != null)
    {
      return !taskThread.isDone();
    }
    return false;
  }
  
  public void startThread()
  {
    stopped = false;
    setStopped(false);
    //taskThread = new Thread(null, this, this.getClass().getSimpleName());
    //taskThread.setDaemon(true);
    taskThread = executorService.submit(this);
    //taskThread.start();
  }
  
  public void close() throws IOException
  {
    stopped = true;
    setStopped(true);
    if (taskThread != null)
    {
      try
      {
        taskThread.cancel(true);
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void setNext(Runnable next)
  {
    this.next = next;
  }
  
  public VTTask addNext(VTTask task)
  {
    this.next = task;
    return task;
  }
  
  public final void run()
  {
    try
    {
      task();
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      stopped = true;
      setStopped(true);
      if (next != null)
      {
        if (next instanceof VTTask)
        {
          ((VTTask)next).startThread();
        }
        else
        {
          executorService.execute(next);
          //nextThread.start();
        }
      }
    }
  }
  
  public abstract void task();
}