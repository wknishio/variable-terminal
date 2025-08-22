package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTHELP extends VTServerStandardLocalConsoleCommandProcessor
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
        VTSystemConsole.print(VTHelpManager.getMainHelpForServerCommands());
      }
      else
      {
        VTSystemConsole.print(VTHelpManager.getMinHelpForServerCommands());
      }
    }
    else if (parsed.length > 1)
    {
      VTSystemConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]));
    }
  }
  
  public void close()
  {
    
  }
}
