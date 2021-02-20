package org.vate.server.console.remote.standard.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTRUNPRINT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTRUNPRINT()
  {
    this.setFullName("*VTRUNPRINT");
    this.setAbbreviatedName("*VTRPR");
    this.setFullSyntax("*VTRUNPRINT <FILE>");
    this.setAbbreviatedSyntax("*VTRPR <FL>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      try
      {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        Method isDesktopSupportedMethod = desktopClass.getDeclaredMethod("isDesktopSupported");
        Method getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop");

        Class<?> memberClasses[] = desktopClass.getClasses();
        Class<?> actionClass = null;

        Object desktopObject = null;
        Object printObject = null;

        for (Class<?> memberClass : memberClasses)
        {
          if (memberClass.getSimpleName().contains("Action"))
          {
            actionClass = memberClass;
            printObject = actionClass.getDeclaredMethod("valueOf", String.class).invoke(null, "PRINT");
          }
        }

        Method isSupportedMethod = desktopClass.getMethod("isSupported", actionClass);
        Method printMethod = desktopClass.getMethod("print", File.class);

        // getDesktopMethod.setAccessible(true);
        // isDesktopSupportedMethod.setAccessible(true);
        // isSupportedMethod.setAccessible(true);
        // printMethod.setAccessible(true);

        if ((Boolean) isDesktopSupportedMethod.invoke(null))
        {
          desktopObject = getDesktopMethod.invoke(null);
        }

        // Class.forName("java.awt.Desktop");
        // Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        // if (desktop != null && desktop.isSupported(Desktop.Action.PRINT))
        if (desktopObject != null && ((Boolean) isSupportedMethod.invoke(desktopObject, printObject)))
        {
          // desktop.print(new File(parsed[1]));
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
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IllegalArgumentException e)
      {
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (IOException e)
      {
        connection.getResultWriter().write("\nVT>Print operation failed!\nVT>");
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
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
}
