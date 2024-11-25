package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

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
      boolean ok = VTConsole.setLogReadLine(parameter);
      if (ok)
      {
        VTConsole.print("\nVT>Enabled recording of client commands to file: [" + parameter + "]\nVT>");
      }
      else
      {
        VTConsole.print("\nVT>Failed recording of client commands to file: [" + parameter + "]\nVT>");
      }
    }
    else
    {
      VTConsole.print("\nVT>Disabled recording of client commands\nVT>");
      VTConsole.setLogReadLine(null);
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