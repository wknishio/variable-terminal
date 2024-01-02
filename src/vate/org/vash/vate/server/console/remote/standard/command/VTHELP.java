package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTHELP extends VTServerStandardRemoteConsoleCommandProcessor
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
        connection.getResultWriter().write(VTHelpManager.getMainHelpForClientCommands());
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write(VTHelpManager.getMinHelpForClientCommands());
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length > 1)
    {
      connection.getResultWriter().write(VTHelpManager.getHelpForClientCommand(parsed[1]));
      connection.getResultWriter().flush();
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
