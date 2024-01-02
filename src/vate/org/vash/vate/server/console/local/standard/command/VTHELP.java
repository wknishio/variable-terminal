package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTHELP extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTHELP()
  {
    this.setFullName("*VTHELP");
    this.setAbbreviatedName("*VTHP");
    this.setFullSyntax("*VTHELP [NAME]");
    this.setAbbreviatedSyntax("*VTHP [NM]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      if (command.toUpperCase().contains("*VTHELP"))
      {
        VTConsole.print(VTHelpManager.getMainHelpForServerCommands());
      }
      else
      {
        VTConsole.print(VTHelpManager.getMinHelpForServerCommands());
      }
    }
    else if (parsed.length > 1)
    {
      VTConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]));
    }
  }
  
  public void close()
  {
    
  }
}
