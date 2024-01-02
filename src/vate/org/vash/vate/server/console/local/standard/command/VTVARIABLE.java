package org.vash.vate.server.console.local.standard.command;

import java.util.Map.Entry;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.nativeutils.VTNativeUtils;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTVARIABLE extends VTServerStandardLocalConsoleCommandProcessor
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
      message.append("\rVT>List of server environment variables:\nVT>");
      for (Entry<String, String> variable : VTNativeUtils.getvirtualenv().entrySet())
      {
        message.append("\nVT>[" + variable.getKey() + "]=[" + variable.getValue() + "]");
      }
      message.append("\nVT>\nVT>End of server environment variables list\nVT>");
      VTConsole.print(message.toString());
    }
    else if (parsed.length == 2)
    {
      String value = VTNativeUtils.getvirtualenv(parsed[1]);
      if (value != null)
      {
        VTConsole.print("\rVT>[" + parsed[1] + "]=[" + value + "]\nVT>");
      }
      else
      {
        VTConsole.print("\rVT>Environment variable [" + parsed[1] + "] not found on server!\nVT>");
      }
    }
    else if (parsed.length >= 3)
    {
      if (VTNativeUtils.putvirtualenv(parsed[1], parsed[2]) == 0)
      {
        VTConsole.print("\rVT>[" + parsed[1] + "]=[" + parsed[2] + "]\nVT>");
      }
      else
      {
        VTConsole.print("\rVT>Environment variable [" + parsed[1] + "] failed to be set to [" + parsed[2] + "] on server!\nVT>");
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
