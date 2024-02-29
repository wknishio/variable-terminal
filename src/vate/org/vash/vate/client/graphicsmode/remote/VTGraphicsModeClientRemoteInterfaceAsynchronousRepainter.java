package org.vash.vate.client.graphicsmode.remote;

public class VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter implements Runnable
{
  private boolean interrupted;
  private VTGraphicsModeClientRemoteInterface remoteInterface;
  private Thread repainterThread;
  // private Object interruptSynchronizer;
  
  public VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter(VTGraphicsModeClientRemoteInterface remoteInterface)
  {
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
      repainterThread = new Thread(null, this, this.getClass().getSimpleName());
      repainterThread.setDaemon(true);
      repainterThread.start();
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
    if (isRunning())
    {
      try
      {
        repainterThread.join();
      }
      catch (InterruptedException e)
      {
        
      }
    }
  }
  
  public boolean isInterrupted()
  {
    return interrupted;
  }
  
  public boolean isRunning()
  {
    if (repainterThread != null && repainterThread.isAlive())
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
          wait(100);
          // System.out.println("async-cycle");
          // System.out.println("async-interrupted:" + interrupted);
          // System.out.println("async-updating:" +
          // remoteInterface.isUpdating());
          if (!interrupted && remoteInterface.isFocusOwner() && remoteInterface.isUpdating())
          {
            remoteInterface.repaint();
            // remoteInterface.redraw();
          }
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
}