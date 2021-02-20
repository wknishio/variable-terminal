package org.vate.server.console.remote.standard.command;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTHELP extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTHELP()
  {
    this.setFullName("*VTHELP");
    this.setAbbreviatedName("*VTHLP");
    this.setFullSyntax("*VTHELP");
    this.setAbbreviatedSyntax("*VTHLP");
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
}
