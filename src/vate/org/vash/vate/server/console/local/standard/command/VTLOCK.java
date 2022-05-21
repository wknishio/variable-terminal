package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTLOCK extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTLOCK()
  {
    this.setFullName("*VTLOCK");
    this.setAbbreviatedName("*VTLK");
    this.setFullSyntax("*VTLOCK <USER PASSWORD>");
    this.setAbbreviatedSyntax("*VTLK <US PW>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 3)
    {
      String user = parsed[1];
      String password = parsed[2];
      server.setUniqueUserCredential(user, password);
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
