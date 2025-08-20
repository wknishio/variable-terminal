package org.vash.vate.client.graphicslink.remote;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VTGraphicsLinkClientRemoteInterfaceAsynchronousRepainter implements Runnable
{
  private volatile boolean interrupted;
  private VTGraphicsLinkClientRemoteInterface remoteInterface;
  private Future<?> repainterThread;
  private ExecutorService executorService;
  // private Object interruptSynchronizer;
  
  public VTGraphicsLinkClientRemoteInterfaceAsynchronousRepainter(VTGraphicsLinkClientRemoteInterface remoteInterface, ExecutorService executorService)
  {
    this.executorService = executorService;
    this.remoteInterface = remoteInterface;
    this.interrupted = false;
    // this.interruptSynchronizer = new Object();
  }
  
  public void start()
  {
    if (isRunning())
    {
      stop();
    }
    interrupted = false;
    remoteInterface.setUpdating(true);
    synchronized (this)
    {
      repainterThread = executorService.submit(this);
    }
  }
  
  public void interrupt()
  {
    interrupted = true;
    synchronized (this)
    {
      notify();
    }
  }
  
  public void resume()
  {
    interrupted = false;
    synchronized (this)
    {
      notify();
    }
  }
  
  public void stop()
  {
    if (isInterrupted())
    {
      resume();
    }
    remoteInterface.setUpdating(false);
    synchronized (this)
    {
      notify();
    }
    try
    {
      repainterThread.get();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public boolean isInterrupted()
  {
    return interrupted;
  }
  
  public boolean isRunning()
  {
    if (repainterThread != null && !repainterThread.isDone())
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public void run()
  {
    try
    {
      while (remoteInterface.isUpdating())
      {
        while (interrupted)
        {
          synchronized (this)
          {
            wait();
          }
        }
        if (!interrupted && remoteInterface.isVisible() && remoteInterface.isUpdating())
        {
          remoteInterface.repaint();
          synchronized (this)
          {
            wait(125);
          }
        }
      }
      
    }
    catch (Throwable e)
    {
      
    }
  }
}