package org.vash.vate.exception;

//import org.vash.vate.console.VTConsole;

public class VTUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
  public void uncaughtException(Thread t, Throwable e)
  {
    if (e instanceof ThreadDeath)
    {
      return;
    }
    //StringBuilder message = new StringBuilder();
    //message.append("\nVT>Uncaught exception!");
    //message.append("\nVT>Thread: " + t.getName());
    //message.append("\nVT>Exception: " + e.toString());
    //message.append("\nVT>StackTrace: ");
    //StackTraceElement[] stackStrace = e.getStackTrace();
    //for (int i = stackStrace.length - 1; i >= 0; i--)
    //{
      //message.append(stackStrace[i].toString() + "\n");
    //}
    // VTConsole.print(message.toString());
  }
}