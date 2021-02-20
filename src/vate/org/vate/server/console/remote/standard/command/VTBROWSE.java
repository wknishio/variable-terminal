package org.vate.server.console.remote.standard.command;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBROWSE extends VTServerStandardRemoteConsoleCommandProcessor
{
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
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        Method isDesktopSupportedMethod = desktopClass.getDeclaredMethod("isDesktopSupported");
        Method getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop");

        Class<?> memberClasses[] = desktopClass.getClasses();
        Class<?> actionClass = null;

        Object desktopObject = null;
        Object browseObject = null;

        for (Class<?> memberClass : memberClasses)
        {
          if (memberClass.getSimpleName().contains("Action"))
          {
            actionClass = memberClass;
            browseObject = actionClass.getDeclaredMethod("valueOf", String.class).invoke(null, "BROWSE");
          }
        }

        Method isSupportedMethod = desktopClass.getMethod("isSupported", actionClass);
        Method browseMethod = desktopClass.getMethod("browse", URI.class);

        // getDesktopMethod.setAccessible(true);
        // isDesktopSupportedMethod.setAccessible(true);
        // isSupportedMethod.setAccessible(true);
        // browseMethod.setAccessible(true);

        if ((Boolean) isDesktopSupportedMethod.invoke(null))
        {
          desktopObject = getDesktopMethod.invoke(null);
        }

        // Class.forName("java.awt.Desktop");
        // Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        // if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
        if (desktopObject != null && ((Boolean) isSupportedMethod.invoke(desktopObject, browseObject)))
        {
          // desktop.browse(new URI(parsed[1]));
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
}
