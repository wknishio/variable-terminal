package org.vate.server.console.local.standard.command;

import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTLOCK extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTLOCK()
  {
    this.setFullName("*VTLOCK");
    this.setAbbreviatedName("*VTLK");
    this.setFullSyntax("*VTLOCK <LOGIN PASSWORD>");
    this.setAbbreviatedSyntax("*VTLK <LGN PW>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 3)
    {
      String login = parsed[1];
      String password = parsed[2];
      server.setUniqueUserCredential(login, password);
      VTConsole.print("\rVT>Single credential set!\nVT>");
    }
    else
    {
      VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
    }
  }

  public void close()
  {

  }
}
