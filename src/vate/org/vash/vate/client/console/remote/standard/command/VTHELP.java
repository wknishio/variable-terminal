package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;

public class VTHELP extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTHELP()
  {
    this.setFullName("*VTHELP");
    this.setAbbreviatedName("*VTHL");
    this.setFullSyntax("*VTHELP [NAME]");
    this.setAbbreviatedSyntax("*VTHL [NM]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      if (command.toUpperCase().contains("*VTHELP"))
      {
        VTConsole.print(VTHelpManager.getMainHelpForClientCommands());
      }
      else
      {
        VTConsole.print(VTHelpManager.getMinHelpForClientCommands());
      }
    }
    else if (parsed.length > 1)
    {
      VTConsole.print(VTHelpManager.getHelpForClientCommand(parsed[1]));
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
