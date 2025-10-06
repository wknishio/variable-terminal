package org.vash.vate.runtime;

public class VTRuntimeProcessTimeoutKill implements Runnable
{
  private boolean running;
  private long timeout;
  // private Thread thread;
  private VTRuntimeProcess process;
  
  public VTRuntimeProcessTimeoutKill(VTRuntimeProcess process, long timeout)
  {
    this.running = true;
    this.process = process;
    this.timeout = timeout;
  }
  
//  public void finalize()
//  {
//    stop();
//  }
  
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