package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTLOCK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTLOCK()
  {
    this.setFullName("*VTLOCK");
    this.setAbbreviatedName("*VTLK");
    this.setFullSyntax("*VTLOCK <USER/PASS>");
    this.setAbbreviatedSyntax("*VTLK <US/PS>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2 && parsed[1].contains("/"))
    {
      int idx = parsed[1].indexOf('/');
      String user = parsed[1].substring(0, idx);
      String password = parsed[1].substring(idx + 1);
      session.getServer().setUniqueUserCredential(user, password);
      connection.getResultWriter().write("\rVT>Single credential set!\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
