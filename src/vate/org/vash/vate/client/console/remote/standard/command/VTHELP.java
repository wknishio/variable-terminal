package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTSystemConsole;
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
        VTSystemConsole.print(VTHelpManager.getMainHelpForClientCommands());
      }
      else
      {
        VTSystemConsole.print(VTHelpManager.getMinHelpForClientCommands());
      }
    }
    else if (parsed.length > 1)
    {
      VTSystemConsole.print(VTHelpManager.getHelpForClientCommand(parsed[1]));
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
