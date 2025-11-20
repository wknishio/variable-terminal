package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;
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
        VTMainConsole.print(VTHelpManager.getMainHelpForClientCommands().substring(1));
      }
      else
      {
        VTMainConsole.print(VTHelpManager.getMinHelpForClientCommands().substring(1));
      }
    }
    else if (parsed.length > 1)
    {
      VTMainConsole.print(VTHelpManager.getHelpForClientCommand(parsed[1]).substring(1));
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
