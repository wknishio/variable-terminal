package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;

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
      boolean ok = VTMainConsole.setLogOutput(parameter);
      if (ok)
      {
        VTMainConsole.print("\rVT>Enabled recording of client console to file: [" + parameter + "]\nVT>");
      }
      else
      {
        VTMainConsole.print("\rVT>Failed recording of client console to file: [" + parameter + "]\nVT>");
      }
    }
    else
    {
      VTMainConsole.print("\rVT>Disabled recording of client console\nVT>");
      VTMainConsole.setLogOutput(null);
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