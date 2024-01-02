package org.vash.vate.server.console.remote.standard.command;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.vash.vate.console.VTConsole;
import org.vash.vate.graphics.message.VTGraphicsMessager;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENALERT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSCREENALERT()
  {
    this.setFullName("*VTSCREENALERT");
    this.setAbbreviatedName("*VTSA");
    this.setFullSyntax("*VTSCREENALERT <ALERT> [TITLE] [DISPLAY]");
    this.setAbbreviatedSyntax("*VTSA <AL> [TI] [DP]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (!VTReflectionUtils.isAWTHeadless())
    {
      if (parsed.length == 2)
      {
        String alert = parsed[1];
        VTGraphicsMessager.showAlert(VTConsole.getFrame(), "Variable-Terminal - Server", alert);
        session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + alert + "]");
        connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
        connection.getResultWriter().flush();
      }
      else if (parsed.length == 3)
      {
        try
        {
          String alert = parsed[1];
          int number = Integer.parseInt(parsed[2]);
          final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
          if (number < devices.length && devices.length > 0)
          {
            if (number <= -1)
            {
              number = 0;
            }
            VTGraphicsMessager.showAlert(devices[number], VTConsole.getFrame(), "Variable-Terminal - Server", alert);
            session.getServer().displayTrayIconMessage("Variable-Terminal - Server", "[" + alert + "]");
            connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (NumberFormatException e)
        {
          String alert = parsed[1];
          String title = parsed[2];
          VTGraphicsMessager.showAlert(VTConsole.getFrame(), title, alert);
          session.getServer().displayTrayIconMessage(title, "[" + alert + "]");
          connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length >= 4)
      {
        try
        {
          String alert = parsed[1];
          String title = parsed[2];
          int number = Integer.parseInt(parsed[3]);
          final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
          if (number < devices.length && devices.length > 0)
          {
            if (number <= -1)
            {
              number = 0;
            }
            VTGraphicsMessager.showAlert(devices[number], VTConsole.getFrame(), title, alert);
            session.getServer().displayTrayIconMessage(title, "[" + alert + "]");
            connection.getResultWriter().write("\nVT>Graphical alert sent to server!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (NumberFormatException e)
        {
          connection.getResultWriter().write("\nVT>Graphical display device not found!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    else
    {
      connection.getResultWriter().write("\nVT>Graphical alert not supported in server!\nVT>");
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
