package org.vash.vate.runtime;

public class VTExit
{
  private static Runnable hook;
  
  public static void installHook(Runnable hook)
  {
    VTExit.hook = hook;
  }
  
  public static void exit(int status)
  {
    if (hook != null)
    {
      hook.run();
    }
    System.exit(status);
  }
}
