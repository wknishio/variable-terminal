package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTSystemConsole;

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
      //String parameter = command.substring(parsed[0].length() + 1);
      String parameter = parsed[1];
      boolean ok = VTSystemConsole.setLogOutput(parameter);
      if (ok)
      {
        VTSystemConsole.print("\nVT>Enabled recording of client console to file: [" + parameter + "]\nVT>");
      }
      else
      {
        VTSystemConsole.print("\nVT>Failed recording of client console to file: [" + parameter + "]\nVT>");
      }
    }
    else
    {
      VTSystemConsole.print("\nVT>Disabled recording of client console\nVT>");
      VTSystemConsole.setLogOutput(null);
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