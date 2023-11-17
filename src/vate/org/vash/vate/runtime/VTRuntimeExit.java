package org.vash.vate.runtime;

public class VTRuntimeExit
{
  private static Runnable hook;
  
  public static void setHook(Runnable hook)
  {
    VTRuntimeExit.hook = hook;
  }
  
  public static void exit(int status)
  {
    if (hook != null)
    {
      try
      {
        hook.run();
      }
      catch (Throwable t)
      {
        
      }
    }
    System.exit(status);
  }
}
