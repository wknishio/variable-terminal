package org.vash.vate.runtime;

public class VTRuntimeProcessTimeoutKill implements Runnable
{
  private volatile boolean running;
  //private volatile long last = 0;
  //private volatile long current = 0;
  //private volatile long elapsed = 0;
  private volatile long timeout;
  // private Thread thread;
  private VTRuntimeProcess process;
  
  public VTRuntimeProcessTimeoutKill(VTRuntimeProcess process, long timeout)
  {
    this.running = true;
    this.process = process;
    this.timeout = timeout;
  }
  
  public void stop()
  {
    running = false;
    synchronized (this)
    {
      notifyAll();
    }
  }
  
  public void kill()
  {
    if (process != null && process.isAlive())
    {
      try
      {
        process.stop();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void finalize()
  {
    stop();
  }
  
  public void run()
  {
    if (running && process != null && process.isAlive())
    {
      synchronized (this)
      {
        try 
        {
          wait(timeout);
        }
        catch (Throwable t)
        {
          
        }
      }
      running = false;
      kill();
    }
  }
}