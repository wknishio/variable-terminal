package org.vash.vate.server.console.remote.standard.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCALLPRINT extends VTServerStandardRemoteConsoleCommandProcessor
{
  private static Class<?> desktopClass;
  private static Method isDesktopSupportedMethod;
  private static Method getDesktopMethod;
  private static Class<?> memberClasses[];
  private static Class<?> actionClass;
  private static Object desktopObject;
  private static Object printObject;
  private static Method isSupportedMethod;
  private static Method printMethod;
  
  private static boolean isSupported = false;
  
  static
  {
    try
    {
      desktopClass = Class.forName("java.awt.Desktop");
      isDesktopSupportedMethod = desktopClass.getDeclaredMethod("isDesktopSupported");
      getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop");
      memberClasses = desktopClass.getClasses();
      actionClass = null;
      desktopObject = null;
      printObject = null;
      for (Class<?> memberClass : memberClasses)
      {
        if (memberClass.getSimpleName().contains("Action"))
        {
          actionClass = memberClass;
          printObject = actionClass.getDeclaredMethod("valueOf", String.class).invoke(null, "PRINT");
        }
      }
      isSupportedMethod = desktopClass.getMethod("isSupported", actionClass);
      printMethod = desktopClass.getMethod("print", File.class);
      // getDesktopMethod.setAccessible(true);
      // isDesktopSupportedMethod.setAccessible(true);
      if ((Boolean) isDesktopSupportedMethod.invoke(null))
      {
        desktopObject = getDesktopMethod.invoke(null);
        if (desktopObject != null && ((Boolean) isSupportedMethod.invoke(desktopObject, printObject)))
        {
          // printMethod.setAccessible(true);
          isSupported = true;
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public VTCALLPRINT()
  {
    this.setFullName("*VTCALLPRINT");
    this.setAbbreviatedName("*VTCPR");
    this.setFullSyntax("*VTCALLPRINT <FILE>");
    this.setAbbreviatedSyntax("*VTCPR <FL>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      try
      {
        // Class.forName("java.awt.Desktop");
        // Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
        // : null;
        // if (desktop != null && desktop.isSupported(Desktop.Action.PRINT))
        if (isSupported)
        {
          // desktop.print(new File(parsed[1]));
          Object desktopObject = getDesktopMethod.invoke(null);
          printMethod.invoke(desktopObject, new File(command.substring(parsed[0].length() + 1)));
          connection.getResultWriter().write("\nVT>Print operation executed!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Print operation not supported!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      catch (SecurityException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IllegalArgumentException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IOException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else
    {
      connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
