package org.vash.vate.server.console.remote.standard.command;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBROWSE extends VTServerStandardRemoteConsoleCommandProcessor
{
  private static Class<?> desktopClass;
  private static Method isDesktopSupportedMethod;
  private static Method getDesktopMethod;
  private static Class<?> memberClasses[];
  private static Class<?> actionClass;
  private static Object desktopObject;
  private static Object browseObject;
  private static Method isSupportedMethod;
  private static Method browseMethod;
  
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
      browseObject = null;
      for (Class<?> memberClass : memberClasses)
      {
        if (memberClass.getSimpleName().contains("Action"))
        {
          actionClass = memberClass;
          browseObject = actionClass.getDeclaredMethod("valueOf", String.class).invoke(null, "BROWSE");
        }
      }
      isSupportedMethod = desktopClass.getMethod("isSupported", actionClass);
      browseMethod = desktopClass.getMethod("browse", URI.class);
      // getDesktopMethod.setAccessible(true);
      // isDesktopSupportedMethod.setAccessible(true);
      // isSupportedMethod.setAccessible(true);
      if ((Boolean) isDesktopSupportedMethod.invoke(null))
      {
        desktopObject = getDesktopMethod.invoke(null);
        if (desktopObject != null && ((Boolean) isSupportedMethod.invoke(desktopObject, browseObject)))
        {
          // browseMethod.setAccessible(true);
          isSupported = true;
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public VTBROWSE()
  {
    this.setFullName("*VTBROWSE");
    this.setAbbreviatedName("*VTBRW");
    this.setFullSyntax("*VTBROWSE <URI>");
    this.setAbbreviatedSyntax("*VTBRW <URI>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      try
      {
        if (isSupported)
        {
          // desktop.browse(new URI(parsed[1]));
          Object desktopObject = getDesktopMethod.invoke(null);
          browseMethod.invoke(desktopObject, new URI(command.substring(parsed[0].length() + 1)));
          connection.getResultWriter().write("\nVT>Browse operation executed!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Browse operation not supported!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      catch (SecurityException e)
      {
        connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IllegalArgumentException e)
      {
        connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IOException e)
      {
        connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        connection.getResultWriter().write("\nVT>Browse operation failed!\nVT>");
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
