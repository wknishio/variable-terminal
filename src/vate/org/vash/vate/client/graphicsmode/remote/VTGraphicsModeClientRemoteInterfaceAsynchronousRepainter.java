package org.vash.vate.client.graphicsmode.remote;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter implements Runnable
{
  private volatile boolean interrupted;
  private VTGraphicsModeClientRemoteInterface remoteInterface;
  private Future<?> repainterThread;
  private ExecutorService executorService;
  // private Object interruptSynchronizer;
  
  public VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter(VTGraphicsModeClientRemoteInterface remoteInterface, ExecutorService executorService)
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
    synchronized (this)
    {
      interrupted = false;
      remoteInterface.setUpdating(true);
      //repainterThread = new Thread(null, this, this.getClass().getSimpleName());
      //repainterThread.setDaemon(true);
      //repainterThread.start();
      repainterThread = executorService.submit(this);
    }
  }
  
  public void interrupt()
  {
    synchronized (this)
    {
      interrupted = true;
      notify();
    }
  }
  
  public void resume()
  {
    synchronized (this)
    {
      interrupted = false;
      notify();
    }
  }
  
  public void stop()
  {
    if (isInterrupted())
    {
      resume();
    }
    synchronized (this)
    {
      remoteInterface.setUpdating(false);
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
      synchronized (this)
      {
        while (remoteInterface.isUpdating())
        {
          while (interrupted)
          {
            wait();
          }
          // System.out.println("async-cycle");
          // System.out.println("async-interrupted:" + interrupted);
          // System.out.println("async-updating:" +
          // remoteInterface.isUpdating());
          if (!interrupted && remoteInterface.isVisible() && remoteInterface.isUpdating())
          {
            remoteInterface.repaint();
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