package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTMainConsole;
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
        VTMainConsole.print(VTHelpManager.getMainHelpForServerCommands().substring(1));
      }
      else
      {
        VTMainConsole.print(VTHelpManager.getMinHelpForServerCommands().substring(1));
      }
    }
    else if (parsed.length > 1)
    {
      VTMainConsole.print(VTHelpManager.getHelpForServerCommand(parsed[1]).substring(1));
    }
  }
  
  public void close()
  {
    
  }
}
