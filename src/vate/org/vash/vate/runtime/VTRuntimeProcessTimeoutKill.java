package org.vash.vate.runtime;

public class VTRuntimeProcessTimeoutKill implements Runnable
{
  private volatile boolean running;
  private volatile long last = 0;
  private volatile long current = 0;
  private volatile long elapsed = 0;
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
    if (!running)
    {
      return;
    }
    running = false;
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
    // thread = Thread.currentThread();
    // thread.setName(getClass().getSimpleName());
    current = System.currentTimeMillis();
    last = current;
    while (running && process != null && process.isAlive())
    {
      try
      {
        Thread.sleep(500);
      }
      catch (Throwable t)
      {
        
      }
      current = System.currentTimeMillis();
      if (current > last)
      {
        elapsed += current - last;
      }
      last = current;
      if (elapsed >= timeout)
      {
        running = false;
      }
    }
    kill();
  }
}