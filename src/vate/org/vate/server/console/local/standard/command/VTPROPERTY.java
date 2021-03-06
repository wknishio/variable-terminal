package org.vate.server.console.local.standard.command;

import java.util.Map.Entry;

import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTPROPERTY extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTPROPERTY()
  {
    this.setFullName("*VTPROPERTY");
    this.setAbbreviatedName("*VTPROP");
    this.setFullSyntax("*VTPROPERTY [NAME] [VALUE]");
    this.setAbbreviatedSyntax("*VTPROP [NM] [VL]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      message.setLength(0);
      message.append("\rVT>JVM properties on server:\nVT>");
      for (Entry<Object, Object> property : System.getProperties().entrySet())
      {
        message.append("\nVT>[" + property.getKey().toString() + "]=[" + property.getValue().toString() + "]");
      }
      message.append("\nVT>\nVT>End of JVM properties list\nVT>");
      VTConsole.print(message.toString());
    }
    else if (parsed.length == 2)
    {
      String value = System.getProperty(parsed[1]);
      if (value != null)
      {
        VTConsole.print("\rVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
      }
      else
      {
        VTConsole.print("\rVT>JVM property [" + parsed[1] + "] not found on server!\nVT>");
      }
    }
    else if (parsed.length >= 3)
    {
      try
      {
        System.setProperty(parsed[1], parsed[2]);
        VTConsole.print("\rVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
      }
      catch (Throwable e)
      {
        VTConsole.print("\rVT>JVM property [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
      }
    }
    else
    {
      VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
    }
  }

  public void close()
  {

  }
}
