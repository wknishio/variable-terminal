package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;

public class VTLOG extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTLOG()
  {
    this.setFullName("*VTLOG");
    this.setAbbreviatedName("*VTLG");
    this.setFullSyntax("*VTLOG [FILE]");
    this.setAbbreviatedSyntax("*VTLG [FL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      //String parameter = command.substring(parsed[0].length() + 1);
      String parameter = parsed[1];
      boolean ok = VTMainConsole.setLogReadLine(parameter);
      if (ok)
      {
        VTMainConsole.print("\rVT>Enabled recording of client commands to file: [" + parameter + "]\nVT>");
      }
      else
      {
        VTMainConsole.print("\rVT>Failed recording of client commands to file: [" + parameter + "]\nVT>");
      }
    }
    else
    {
      VTMainConsole.print("\rVT>Disabled recording of client commands\nVT>");
      VTMainConsole.setLogReadLine(null);
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