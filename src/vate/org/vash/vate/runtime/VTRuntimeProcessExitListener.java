package org.vash.vate.runtime;

public class VTRuntimeProcessExitListener implements Runnable
{
  private final VTRuntimeProcess process;
  
  public VTRuntimeProcessExitListener(VTRuntimeProcess process)
  {
    this.process = process;
  }
  
//  public void finalize()
//  {
//    if (process != null && process.isAlive())
//    {
//      process.destroy();
//    }
//  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      process.waitFor();
    }
    catch (Throwable e)
    {
      
    }
    
    if (process.isRestart())
    {
      try
      {
        Thread.sleep(125);
      }
      catch (Throwable e)
      {
        
      }
      while (!process.restart())
      {
        try
        {
          Thread.sleep(125);
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    else
    {
      process.stop();
    }
  }
}