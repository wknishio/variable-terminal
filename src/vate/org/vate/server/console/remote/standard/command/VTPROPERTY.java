package org.vate.server.console.remote.standard.command;

import java.util.Map.Entry;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPROPERTY extends VTServerStandardRemoteConsoleCommandProcessor
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
      message.append("\nVT>List of JVM properties on server:\nVT>");
      for (Entry<Object, Object> property : System.getProperties().entrySet())
      {
        message.append("\nVT>[" + property.getKey().toString() + "]=[" + property.getValue().toString() + "]");
      }
      message.append("\nVT>\nVT>End of JVM properties list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
    }
    else if (parsed.length == 2)
    {
      String value = System.getProperty(parsed[1]);
      if (value != null)
      {
        connection.getResultWriter().write("\nVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>JVM property [" + parsed[1] + "] not found on server!\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length >= 3)
    {
      try
      {
        System.setProperty(parsed[1], parsed[2]);
        connection.getResultWriter().write("\nVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        connection.getResultWriter().write("\nVT>JVM property [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
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
