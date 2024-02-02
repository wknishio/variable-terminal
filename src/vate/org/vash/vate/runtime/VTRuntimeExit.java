package org.vash.vate.runtime;

import java.util.ArrayList;
import java.util.List;

public class VTRuntimeExit
{
  private static List<Runnable> hooks = new ArrayList<Runnable>();
  
  public static int addHook(Runnable hook)
  {
    hooks.remove(hook);
    hooks.add(hook);
    return hooks.size() - 1;
  }
  
  public static boolean removeHook(int index)
  {
    return hooks.remove(index) != null;
  }
  
  public static boolean removeHook(Runnable hook)
  {
    return hooks.remove(hook);
  }
  
  public static void exit(int status)
  {
    if (hooks.size() > 0)
    {
      for (Runnable hook : hooks.toArray(new Runnable[] {}))
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
      }
    }
    System.exit(status);
  }
}
