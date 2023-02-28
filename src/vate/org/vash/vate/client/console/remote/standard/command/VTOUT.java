package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTOUT extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTOUT()
  {
    this.setFullName("*VTOUT");
    this.setAbbreviatedName("*VTOT");
    this.setFullSyntax("*VTOUT [FILE]");
    this.setAbbreviatedSyntax("*VTOT [FL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      String parameter = command.substring(parsed[0].length() + 1);
      boolean ok = VTConsole.setLogOutput(parameter);
      if (ok)
      {
        VTConsole.print("\nVT>Enabled recording of client console to file: [" + parameter + "]\nVT>");
      }
      else
      {
        VTConsole.print("\nVT>Failed recording of client console to file: [" + parameter + "]\nVT>");
      }
    }
    else
    {
      VTConsole.print("\nVT>Disabled recording of client console\nVT>");
      VTConsole.setLogOutput(null);
    }
  }
  
  public void close()
  {
    
  }
}