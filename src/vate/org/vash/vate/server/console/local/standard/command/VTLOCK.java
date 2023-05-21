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
    this.setFullSyntax("*VTLOCK <USER/PASS>");
    this.setAbbreviatedSyntax("*VTLK <US/PW>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2 && parsed[1].contains("/"))
    {
      int idx = parsed[1].indexOf('/');
      String user = parsed[1].substring(0, idx);
      String password = parsed[1].substring(idx + 1);
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
