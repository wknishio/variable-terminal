package org.vash.vate.server.console.remote.standard.command;

import java.util.Map.Entry;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.nativeutils.VTSystemNativeUtils;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTVARIABLE extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTVARIABLE()
  {
    this.setFullName("*VTVARIABLE");
    this.setAbbreviatedName("*VTVB");
    this.setFullSyntax("*VTVARIABLE [NAME] [VALUE]");
    this.setAbbreviatedSyntax("*VTVB [NM] [VL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      message.setLength(0);
      message.append("\nVT>List of server environment variables:\nVT>");
      for (Entry<String, String> variable : VTSystemNativeUtils.getvirtualenv().entrySet())
      {
        message.append("\nVT>[" + variable.getKey() + "]=[" + variable.getValue() + "]");
      }
      message.append("\nVT>\nVT>End of server environment variables list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
    }
    else if (parsed.length == 2)
    {
      String value = VTSystemNativeUtils.getvirtualenv(parsed[1]);
      if (value != null)
      {
        connection.getResultWriter().write("\nVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Environment variable [" + parsed[1] + "] not found on server!\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length >= 3)
    {
      if (VTSystemNativeUtils.putvirtualenv(parsed[1], parsed[2]) == 0)
      {
        connection.getResultWriter().write("\nVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Environment variable [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
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
