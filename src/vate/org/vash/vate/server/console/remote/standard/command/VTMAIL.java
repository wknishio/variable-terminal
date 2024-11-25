package org.vash.vate.server.console.remote.standard.command;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTMAIL extends VTServerStandardRemoteConsoleCommandProcessor
{
  private static Class<?> desktopClass;
  private static Method isDesktopSupportedMethod;
  private static Method getDesktopMethod;
  private static Class<?> memberClasses[];
  private static Class<?> actionClass;
  private static Object desktopObject;
  private static Object mailObject;
  private static Method isSupportedMethod;
  private static Method mailMethodVoid;
  private static Method mailMethodURI;
  
  private static boolean isSupported = false;
  
  public VTMAIL()
  {
    this.setFullName("*VTMAIL");
    this.setAbbreviatedName("*VTML");
    this.setFullSyntax("*VTMAIL [URI]");
    this.setAbbreviatedSyntax("*VTML [UR]");
  }
  
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
      mailObject = null;
      for (Class<?> memberClass : memberClasses)
      {
        if (memberClass.getSimpleName().contains("Action"))
        {
          actionClass = memberClass;
          mailObject = actionClass.getDeclaredMethod("valueOf", String.class).invoke(null, "MAIL");
        }
      }
      isSupportedMethod = desktopClass.getMethod("isSupported", actionClass);
      mailMethodVoid = desktopClass.getMethod("mail");
      mailMethodURI = desktopClass.getMethod("mail", URI.class);
      // getDesktopMethod.setAccessible(true);
      // isDesktopSupportedMethod.setAccessible(true);
      if ((Boolean) isDesktopSupportedMethod.invoke(null))
      {
        desktopObject = getDesktopMethod.invoke(null);
        if (desktopObject != null && ((Boolean) isSupportedMethod.invoke(desktopObject, mailObject)))
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
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 1)
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
          if (parsed.length <= 1)
          {
            mailMethodVoid.invoke(desktopObject);
          }
          else if (parsed.length >= 2)
          {
            //mailMethodURI.invoke(desktopObject, new URI("mailto:" + command.substring(parsed[0].length() + 1)));
            mailMethodURI.invoke(desktopObject, new URI("mailto:" + parsed[1]));
          }
          connection.getResultWriter().write("\nVT>Mail operation executed!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Mail operation not supported!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      catch (SecurityException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Mail operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IllegalArgumentException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Mail operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IOException e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Mail operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        connection.getResultWriter().write("\nVT>Mail operation failed!\nVT>");
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
