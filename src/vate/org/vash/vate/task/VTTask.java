package org.vash.vate.task;

import java.io.Closeable;
import java.io.IOException;

public abstract class VTTask implements Runnable, Closeable
{
  protected volatile boolean stopped;
  private Thread taskThread;
  
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
      taskThread.interrupt();
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
        taskThread.join();
      }
      catch (InterruptedException e)
      {
        
      }
    }
  }
  
  public boolean aliveThread()
  {
    if (taskThread != null)
    {
      return taskThread.isAlive();
    }
    return false;
  }
  
  public void startThread()
  {
    // setStopped(false);
    stopped = false;
    taskThread = new Thread(null, this, this.getClass().getSimpleName());
    taskThread.setDaemon(true);
    taskThread.start();
  }
  
  public abstract void run();
  
  public void close() throws IOException
  {
    this.setStopped(true);
    if (taskThread != null)
    {
      try
      {
        taskThread.interrupt();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
}